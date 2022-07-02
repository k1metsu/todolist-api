package dev.kimetsu.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TaskDto {

    Long id;

    String taskName;

    String description;

    @JsonProperty("up_task_id")
    Long upTaskId;

    @JsonProperty("down_task_id")
    Long downTaskId;

    @JsonProperty("created")
    Instant created;
}
