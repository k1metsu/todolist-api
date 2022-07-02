package dev.kimetsu.api.factory;

import dev.kimetsu.api.dto.ProjectDto;
import dev.kimetsu.store.entity.Project;
import org.springframework.stereotype.Component;

@Component
public class ProjectFactory {

    public ProjectDto makeProjectDto(Project project) {

        return ProjectDto.builder()
                .id(project.getId())
                .title(project.getTitle())
                .created(project.getCreated())
                .updated(project.getUpdated())
                .build();

    }

}
