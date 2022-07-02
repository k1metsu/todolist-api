package dev.kimetsu.api.controller;


import dev.kimetsu.api.controller.helper.HelperController;
import dev.kimetsu.api.dto.MessageDto;
import dev.kimetsu.api.dto.TaskDto;
import dev.kimetsu.api.exception.BadRequestException;
import dev.kimetsu.api.factory.TaskFactory;
import dev.kimetsu.store.entity.Project;
import dev.kimetsu.store.entity.Task;
import dev.kimetsu.store.entity.TaskState;
import dev.kimetsu.store.repository.TaskRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
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
public class TaskController {

    TaskRepository taskRepository;
    TaskFactory taskFactory;
    HelperController helperController;

    public static final String GET_TASKS = "/api/v1/projects/{project_id}/task-state/{task_state_id}/tasks";
    public static final String CREATE_TASK= "/api/v1/projects/{project_id}/task-state/{task_state_id}/tasks";
    public static final String UPDATE_TASKS = "/api/v1/tasks/{task_id}";
    public static final String DELETE_TASK = "/api/v1/tasks/{task_id}";

    @GetMapping(GET_TASKS)
    public List<TaskDto> getTasks(
            @PathVariable("project_id") Long projectId,
            @PathVariable("task_state_id") Long taskStateId
    ) {

        Project project = helperController.getProjectOrThrowException(projectId);
        TaskState taskState = helperController.getTaskStateOrThrowException(taskStateId);

        return taskState
                .getTasks()
                .stream()
                .map(taskFactory::makeTaskDto)
                .collect(Collectors.toList());
    }

    @PostMapping(CREATE_TASK)
    public TaskDto createTask(
            @PathVariable("project_id") Long projectId,
            @PathVariable("task_state_id") Long taskStateId,
            @RequestParam("task_name") String taskName,
            @RequestParam("description") String description
            ) {

        Project project = helperController.getProjectOrThrowException(projectId);
        TaskState taskState = helperController.getTaskStateOrThrowException(taskStateId);

        if(taskName.trim().isEmpty()) {
            throw new BadRequestException("Task name can`t be empty");
        }

        Optional<Task> optionalTask = Optional.empty();

        for(Task task: taskState.getTasks()) {

            if(task.getTaskName().equalsIgnoreCase(taskName)) {
                throw new BadRequestException(
                        String.format("Task with task name \"%s\" already exist!", taskName)
                );
            }

            if(!task.getDownTask().isPresent()) {
                optionalTask = Optional.of(task);
                break;
            }

        }

        final Task task = taskRepository.saveAndFlush(
                Task.builder()
                        .taskName(taskName)
                        .description(description)
                        .taskState(taskState)
                        .build()
        );

        optionalTask
                .ifPresent(anotherTask -> {
                    task.setUpTask(anotherTask);
                    anotherTask.setDownTask(task);
                    taskRepository.saveAndFlush(anotherTask);
                });

        final Task saved = taskRepository.saveAndFlush(task);

        return taskFactory.makeTaskDto(saved);
    }


    @PatchMapping(UPDATE_TASKS)
    public TaskDto updateTask(
            @PathVariable("task_id") Long taskId,
            @RequestParam("task_name") String taskName,
            @RequestParam("description") String description) {

        Task task = helperController.getTaskOrThrowException(taskId);

        if(taskName.trim().isEmpty()) {
            throw new BadRequestException("Task name can`t be empty");
        }

        taskRepository
                .findTaskByTaskStateIdAndTaskNameContainingIgnoreCase(task.getTaskState().getId(), taskName)
                .filter(anotherTask -> !anotherTask.getId().equals(taskId))
                .ifPresent(anotherTask -> {
                    throw new BadRequestException("Task already exist");
                });

        task.setTaskName(taskName);
        task.setDescription(description);

        final Task saved = taskRepository.saveAndFlush(task);

        return taskFactory.makeTaskDto(saved);
    }


    @DeleteMapping(DELETE_TASK)
    public MessageDto deleteTaskDELETE_TASKS(@PathVariable("task_id") Long taskId) {
        helperController.getTaskOrThrowException(taskId);
        taskRepository.deleteById(taskId);
        return new MessageDto("task successfully deleted");
    }
}
