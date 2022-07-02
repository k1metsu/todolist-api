package dev.kimetsu.api.controller;

import dev.kimetsu.api.controller.helper.HelperController;
import dev.kimetsu.api.dto.MessageDto;
import dev.kimetsu.api.dto.ProjectDto;
import dev.kimetsu.api.exception.BadRequestException;
import dev.kimetsu.api.factory.ProjectFactory;
import dev.kimetsu.store.entity.Project;
import dev.kimetsu.store.repository.ProjectRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@RequiredArgsConstructor
@RestController
@Transactional
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProjectController {

    ProjectRepository projectRepository;
    ProjectFactory projectFactory;
    HelperController helperController;

    public static final String GET_PROJECTS = "/api/v1/projects";
    public static final String CREATE_OR_UPDATE_PROJECT = "/api/v1/projects";
    public static final String DELETE_PROJECT = "/api/v1/projects/{project_id}";

    @GetMapping(GET_PROJECTS)
    public ResponseEntity<List<ProjectDto>> getProjects(
            @RequestParam("prefix") Optional<String> optionalTitle) {

        optionalTitle = optionalTitle.filter(title -> !title.trim().isEmpty());

        Stream<Project> projectStream = optionalTitle
                .map(projectRepository::streamAllByTitleStartingWithIgnoreCase)
                .orElseGet(projectRepository::streamAllBy);

        List<ProjectDto> listProject = projectStream
                .map(projectFactory::makeProjectDto)
                .collect(Collectors.toList());

        log.info("get all project");

        return new ResponseEntity<>(listProject, HttpStatus.OK);
    }

    @PutMapping(CREATE_OR_UPDATE_PROJECT)
    public ProjectDto createOrUpdateProject(
            @RequestParam(value = "project_id", required = false) Optional<Long> optionalProjectId,
            @RequestParam(value = "project_title", required = false) Optional<String> optionalProjectName) {

        optionalProjectName = optionalProjectName.filter(projectName -> !projectName.trim().isEmpty());

        boolean isCreate = !optionalProjectId.isPresent();

        if (isCreate && !optionalProjectName.isPresent()) {
            throw new BadRequestException("Project name can't be empty.");
        }

        final Project project = optionalProjectId
                .map(helperController::getProjectOrThrowException)
                .orElseGet(() -> Project.builder().build());

        optionalProjectName
                .ifPresent(projectTitle -> {

                    projectRepository
                            .findByTitle(projectTitle)
                            .filter(anotherProject -> !Objects.equals(anotherProject.getId(), project.getId()))
                            .ifPresent(anotherProject -> {
                                throw new BadRequestException(
                                        String.format("Project \"%s\" already exists.", projectTitle)
                                );
                            });

                    project.setTitle(projectTitle);
                });

        final Project savedProject = projectRepository.saveAndFlush(project);

        return projectFactory.makeProjectDto(savedProject);
    }

    @DeleteMapping(DELETE_PROJECT)
    public ResponseEntity<MessageDto> deleteProject(@PathVariable("project_id") Long projectId) {
        helperController.getProjectOrThrowException(projectId);
        projectRepository.deleteById(projectId);
        return ResponseEntity.ok(new MessageDto("Project successfully deleted"));
    }
}
