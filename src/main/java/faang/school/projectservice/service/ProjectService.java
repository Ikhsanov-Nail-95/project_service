package faang.school.projectservice.service;

import faang.school.projectservice.dto.filter.ProjectFilterDto;
import faang.school.projectservice.dto.moment.MomentDto;
import faang.school.projectservice.dto.project.ProjectDto;
import faang.school.projectservice.dto.project.ProjectEvent;
import faang.school.projectservice.exception.DataValidationException;
import faang.school.projectservice.filter.project.ProjectFilter;
import faang.school.projectservice.jpa.ProjectJpaRepository;
import faang.school.projectservice.mapper.ProjectMapper;
import faang.school.projectservice.model.Project;
import faang.school.projectservice.publisher.ProjectEventPublisher;
import faang.school.projectservice.validator.ProjectValidator;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
@RequiredArgsConstructor
@Service
public class ProjectService {
    private final ProjectJpaRepository projectJpaRepository;
    private final ProjectMapper projectMapper;
    private final ProjectValidator projectValidator;
    private final ProjectEventPublisher projectEventPublisher;
    private final List<ProjectFilter> projectFilters;
    @Value("#{new java.math.BigInteger('${storage.max_capacity}')}")
    private BigInteger maxCapacity;

    public ProjectDto createProject(ProjectDto projectDto, long requestUserId) {
        if (projectJpaRepository.existsByOwnerIdAndName(requestUserId, projectDto.getName())) {
            throw new IllegalStateException("User ID " + requestUserId + " is already a member of the team associated with the existing project.");
        }

        Project projectToSave = projectMapper.toEntity(projectDto, requestUserId);
        projectToSave.setMaxStorageSize(maxCapacity);
        Project savedProject = projectJpaRepository.save(projectToSave);

        ProjectEvent projectEvent = new ProjectEvent(savedProject.getOwnerId(), savedProject.getId());
        projectEventPublisher.publish(projectEvent);

        return projectMapper.toDto(savedProject);
    }

    public ProjectDto updateProject(long projectId, ProjectDto projectDto, long requestUserId) {
        Project existingProject = findProjectByIdOrThrow(projectId);

        projectValidator.checkUserIsMemberOrThrowException(existingProject, requestUserId);

        existingProject.setDescription(projectDto.getDescription() == null ? existingProject.getDescription() : projectDto.getDescription());
        existingProject.setStatus(projectDto.getStatus() == null ? existingProject.getStatus() : projectDto.getStatus());

        projectJpaRepository.save(existingProject);

        return projectMapper.toDto(existingProject);
    }

    @Transactional
    public List<ProjectDto> findAllProjectsByFilters(ProjectFilterDto filters, long requestUserId) {
        List<Project> projectList = projectJpaRepository.findProjectByOwnerIdAndTeamMember(requestUserId);
        if (projectList.isEmpty()) {
            throw new EntityNotFoundException("No projects found");
        }

        Stream<Project> projectStream = projectList.stream();

        for (ProjectFilter filter : projectFilters) {
            if (filter.isApplicable(filters)) {
                projectStream = filter.apply(projectStream, filters).stream();
            }
        }
        return projectStream.map(projectMapper::toDto).toList();
    }

    public List<ProjectDto> getAllProjects(long requestUserId) {
        List<Project> projectList = projectJpaRepository.findProjectByOwnerIdAndTeamMember(requestUserId);
        if (projectList.isEmpty()) {
            throw new EntityNotFoundException("No projects found");
        }
        return projectMapper.toDtoList(projectList);
    }

    public ProjectDto getProject(long projectId, long requestUserId) {
        Project project = findProjectByIdOrThrow(projectId);

        projectValidator.checkUserIsMemberOrThrowException(project, requestUserId);

        return projectMapper.toDto(project);
    }

    Project getProject(long projectId) {
        return findProjectByIdOrThrow(projectId);
    }

    Project save(Project project) {
        return projectJpaRepository.save(project);
    }

    public List<Project> getMomentProjectsEntity(MomentDto momentDto) {
        List<Project> projectList = projectJpaRepository.findAllById(momentDto.getProjectIds());
        if (projectList.size() != momentDto.getProjectIds().size()) {
            throw new DataValidationException("Project does not exist");
        }
        return projectList;
    }

    private Project findProjectByIdOrThrow(long projectId) {
        return projectJpaRepository.findById(projectId)
                .orElseThrow(() -> {
                    log.warn("Project not found: ID={}", projectId);
                    return new EntityNotFoundException("Project not found");
                });
    }

}
