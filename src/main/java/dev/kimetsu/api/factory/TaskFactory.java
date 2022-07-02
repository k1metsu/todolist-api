package dev.kimetsu.api.factory;

import dev.kimetsu.api.dto.TaskDto;
import dev.kimetsu.store.entity.Task;
import org.springframework.stereotype.Component;

@Component
public class TaskFactory {

    public TaskDto makeTaskDto(Task task) {

        return TaskDto.builder()
                .id(task.getId())
                .taskName(task.getTaskName())
                .description(task.getDescription())
                .upTaskId(task.getUpTask().map(Task::getId).orElse(null))
                .downTaskId(task.getUpTask().map(Task::getId).orElse(null))
                .created(task.getCreated())
                .build();

    }
}
