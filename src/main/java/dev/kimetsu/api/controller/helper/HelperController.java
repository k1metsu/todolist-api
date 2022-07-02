package dev.kimetsu.api.controller.helper;


import dev.kimetsu.api.exception.NotFoundException;
import dev.kimetsu.store.entity.Project;
import dev.kimetsu.store.entity.Task;
import dev.kimetsu.store.entity.TaskState;
import dev.kimetsu.store.repository.ProjectRepository;
import dev.kimetsu.store.repository.TaskRepository;
import dev.kimetsu.store.repository.TaskStateRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Component
@Transactional
public class HelperController {

    ProjectRepository projectRepository;
    TaskStateRepository taskStateRepository;
    TaskRepository taskRepository;

    public TaskState getTaskStateOrThrowException(Long taskStateId) {

        return taskStateRepository
                .findById(taskStateId)
                .orElseThrow(() ->
                        new NotFoundException(
                                String.format(
                                        "Task state with \"%s\" id doesn't exist.",
                                        taskStateId
                                )
                        )
                );
    }

    public Project getProjectOrThrowException(Long projectId) {

        return projectRepository
                .findById(projectId)
                .orElseThrow(() ->
                        new NotFoundException(
                                String.format(
                                        "Project with \"%s\" doesn't exist.",
                                        projectId
                                )
                        )
                );
    }

    public Task getTaskOrThrowException(Long taskId) {

        return taskRepository
                .findById(taskId)
                .orElseThrow(() ->
                        new NotFoundException(
                                String.format(
                                        "Task with \"%s\" doesn't exist.",
                                        taskId
                                )
                        )
                );
    }

}
