package dev.kimetsu.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProjectDto {

    @NonNull
    Long id;

    @NonNull
    String title;

    @NonNull
    @JsonProperty("created_at")
    Instant created;

    @NonNull
    @JsonProperty("updated_at")
    Instant updated;
}
