package dev.kimetsu.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TaskStateDto {

    Long id;
    String stateName;

    @JsonProperty("left_task_state_id")
    Long leftTaskStateId;
    @JsonProperty("right_task_state_id")
    Long rightTaskStateId;

    @NonNull
    @JsonProperty("created")
    Instant created;

    List<TaskDto> tasks;
}
