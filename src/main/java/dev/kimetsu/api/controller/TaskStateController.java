package dev.kimetsu.api.controller;

import dev.kimetsu.api.controller.helper.HelperController;
import dev.kimetsu.api.dto.MessageDto;
import dev.kimetsu.api.dto.TaskStateDto;
import dev.kimetsu.api.exception.BadRequestException;
import dev.kimetsu.api.factory.TaskStateFactory;
import dev.kimetsu.store.entity.Project;
import dev.kimetsu.store.entity.TaskState;
import dev.kimetsu.store.repository.TaskStateRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@RestController
@Transactional
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TaskStateController {

    TaskStateRepository taskStateRepository;
    TaskStateFactory taskStateFactory;
    HelperController helperController;

    public static final String GET_TASK_STATE = "/api/v1/projects/{project_id}/task-state";
    public static final String CREATE_TASK_STATE = "/api/v1/projects/{project_id}/task-state";
    public static final String UPDATE_TASK_STATE = "/api/v1/task-state/{task_state_id}";
    public static final String CHANGE_POSITION_TASK_STATE = "/api/v1/task-state/{task_state_id}/position/change";
    public static final String DELETE_TASK_STATE = "/api/v1/task-state/{task_state_id}";

    @GetMapping(GET_TASK_STATE)
    public List<TaskStateDto> getTaskState(@PathVariable("project_id") Long projectId) {

        Project project = helperController.getProjectOrThrowException(projectId);

        return project
                .getTaskStates()
                .stream()
                .map(taskStateFactory::makeTaskStateDto)
                .collect(Collectors.toList());
    }

    @PostMapping(CREATE_TASK_STATE)
    public TaskStateDto createTaskState(
            @PathVariable("project_id") Long projectId,
            @RequestParam("task_state_name") String stateName) {

        Project project = helperController.getProjectOrThrowException(projectId);

        if(stateName.trim().isEmpty()) {
            throw new BadRequestException("Task State can`t be empty");
        }

        Optional<TaskState> optionalTaskState = Optional.empty();

        for(TaskState taskState: project.getTaskStates()) {

            if(taskState.getStateName().equalsIgnoreCase(stateName)) {
                throw new BadRequestException(
                        String.format("Task state with name \"%s\" already exist", stateName)
                );
            }

            if(!taskState.getRightTaskState().isPresent()) {
                optionalTaskState = Optional.of(taskState);
                break;
            }
        }

        final TaskState taskState = taskStateRepository.saveAndFlush(
                TaskState
                        .builder()
                        .stateName(stateName)
                        .project(project)
                        .build()
        );

        optionalTaskState
                .ifPresent(anotherTaskState -> {
                    taskState.setLeftTaskState(anotherTaskState);
                    anotherTaskState.setRightTaskState(taskState);
                    taskStateRepository.saveAndFlush(anotherTaskState);
                });

        final TaskState saved = taskStateRepository.saveAndFlush(taskState);

        return taskStateFactory.makeTaskStateDto(saved);
    }

    @PatchMapping(UPDATE_TASK_STATE)
    public TaskStateDto patchTaskState(
            @PathVariable("task_state_id") Long taskStateId,
            @RequestParam("task-state-name") String taskStateName)
    {

        if(taskStateName.trim().isEmpty()) {
            throw new BadRequestException("Task state can`t be empty");
        }

        TaskState taskState = helperController.getTaskStateOrThrowException(taskStateId);

        taskStateRepository
                .findTaskStateByProjectIdAndStateNameContainsIgnoreCase(
                        taskState.getProject().getId(),
                        taskStateName
                ).filter(anotherTaskState -> !anotherTaskState.getId().equals(taskStateId))
                .ifPresent(anotherTaskState -> {
                    throw new BadRequestException("Task state already exist");
                });

        taskState.setStateName(taskStateName);

        final TaskState saved = taskStateRepository.saveAndFlush(taskState);

        return taskStateFactory.makeTaskStateDto(saved);

    }

    @PatchMapping(CHANGE_POSITION_TASK_STATE)
    public TaskStateDto changePositionTaskState(
            @PathVariable("task_state_id") Long taskStateId,
            @RequestParam("leftTaskStateId") Optional<Long> optionalLeftTaskStateId
    ) {

        TaskState changeTaskState = helperController.getTaskStateOrThrowException(taskStateId);

        Project project = changeTaskState.getProject();

        Optional<Long> oldLeftTaskStateId = changeTaskState.getLeftTaskState()
                .map(TaskState::getId);

        if(oldLeftTaskStateId.equals(optionalLeftTaskStateId)) {
            return taskStateFactory.makeTaskStateDto(changeTaskState);
        }

        Optional<TaskState> optionalNewLeftTaskState = optionalLeftTaskStateId
                .map(leftTaskStateId -> {
                    if(taskStateId.equals(leftTaskStateId)) {
                        throw new BadRequestException("Left Task state id equals change task state");
                    }

                    TaskState leftTaskState = helperController.getTaskStateOrThrowException(leftTaskStateId);

                    if(!project.getId().equals(leftTaskState.getProject().getId())) {
                        throw new BadRequestException("Task state position can be changed within the same project.");
                    }

                    return leftTaskState;
                });

        Optional<TaskState> optionalNewRightTaskState;
        if (!optionalNewLeftTaskState.isPresent()) {

            optionalNewRightTaskState = project
                    .getTaskStates()
                    .stream()
                    .filter(anotherTaskState -> !anotherTaskState.getLeftTaskState().isPresent())
                    .findAny();
        } else {

            optionalNewRightTaskState = optionalNewLeftTaskState
                    .get()
                    .getRightTaskState();
        }

        replaceOldTaskStatePosition(changeTaskState);

        if (optionalNewLeftTaskState.isPresent()) {

            TaskState newLeftTaskState = optionalNewLeftTaskState.get();

            newLeftTaskState.setRightTaskState(changeTaskState);

            changeTaskState.setLeftTaskState(newLeftTaskState);
        } else {
            changeTaskState.setLeftTaskState(null);
        }

        if (optionalNewRightTaskState.isPresent()) {

            TaskState newRightTaskState = optionalNewRightTaskState.get();

            newRightTaskState.setLeftTaskState(changeTaskState);

            changeTaskState.setRightTaskState(newRightTaskState);
        } else {
            changeTaskState.setRightTaskState(null);
        }

        changeTaskState = taskStateRepository.saveAndFlush(changeTaskState);

        optionalNewLeftTaskState
                .ifPresent(taskStateRepository::saveAndFlush);

        optionalNewRightTaskState
                .ifPresent(taskStateRepository::saveAndFlush);

        return taskStateFactory.makeTaskStateDto(changeTaskState);
    }

    @DeleteMapping(DELETE_TASK_STATE)
    public ResponseEntity<MessageDto> deleteTaskState(@PathVariable("task_state_id") Long taskStateId) {
        TaskState taskState = helperController.getTaskStateOrThrowException(taskStateId);

        replaceOldTaskStatePosition(taskState);

        taskStateRepository.deleteById(taskStateId);

        return ResponseEntity.ok(new MessageDto("Task State success deleted"));
    }

    private void replaceOldTaskStatePosition(TaskState changeTaskState) {

        Optional<TaskState> optionalOldLeftTaskState = changeTaskState.getLeftTaskState();
        Optional<TaskState> optionalOldRightTaskState = changeTaskState.getRightTaskState();

        optionalOldLeftTaskState
                .ifPresent(it -> {

                    it.setRightTaskState(optionalOldRightTaskState.orElse(null));

                    taskStateRepository.saveAndFlush(it);
                });

        optionalOldRightTaskState
                .ifPresent(it -> {

                    it.setLeftTaskState(optionalOldLeftTaskState.orElse(null));

                    taskStateRepository.saveAndFlush(it);
                });
    }


}
