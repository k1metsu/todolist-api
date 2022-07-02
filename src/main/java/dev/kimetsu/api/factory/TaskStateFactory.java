package dev.kimetsu.api.factory;

import dev.kimetsu.api.dto.TaskStateDto;
import dev.kimetsu.store.entity.TaskState;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TaskStateFactory {

    TaskFactory taskFactory;

    public TaskStateDto makeTaskStateDto(TaskState taskState) {

        return TaskStateDto.builder()
                .id(taskState.getId())
                .stateName(taskState.getStateName())
                .created(taskState.getCreated())
                .leftTaskStateId(taskState.getLeftTaskState().map(TaskState::getId).orElse(null))
                .rightTaskStateId(taskState.getRightTaskState().map(TaskState::getId).orElse(null))
                .tasks(
                        taskState
                                .getTasks()
                                .stream()
                                .map(taskFactory::makeTaskDto)
                                .collect(Collectors.toList())
                )
                .build();

    }
}
