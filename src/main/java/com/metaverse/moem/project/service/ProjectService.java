package com.metaverse.moem.project.service;

import com.metaverse.moem.project.domain.Project;
import com.metaverse.moem.project.domain.ProjectType;
import com.metaverse.moem.project.dto.ProjectDto;
import com.metaverse.moem.project.repository.ProjectRepository;
import com.metaverse.moem.team.domain.Team;
import com.metaverse.moem.team.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final TeamRepository teamRepository;

    public ProjectDto.Res create(ProjectDto.CreateReq req) {
        Team team = teamRepository.findById(req.teamId())
                .orElseThrow(() -> new IllegalArgumentException("팀을 찾을 수 없습니다."));

        Project project = Project.builder()
                .name(req.name())
                .type(req.type())
                .ownerId(req.ownerId())
                .team(team)
                .isDeleted(false)
                .build();

        Project saved = projectRepository.save(project);
        return toRes(saved);
    }

    public Optional<ProjectDto.Res> get(Long id) {
        return projectRepository.findById(id)
                .filter(project -> !project.isDeleted())
                .map(this::toRes);
    }

    public List<ProjectDto.Res> listByOwner(ProjectType type, Long ownerId) {
        return projectRepository.findByTypeAndOwnerIdAndIsDeletedFalse(type, ownerId)
                .stream()
                .map(this::toRes)
                .toList();
    }


    public Optional<ProjectDto.Res> update(Long id, ProjectDto.UpdateReq req) {
        return projectRepository.findById(id)
                .filter(project -> !project.isDeleted())
                .map(project -> {
                    project.setName(req.name());
                    return toRes(projectRepository.save(project));
                });
    }

    public boolean delete(Long id) {
        return projectRepository.findById(id)
                .filter(project -> !project.isDeleted())
                .map(project -> {
                    project.setDeleted(true);
                    projectRepository.save(project);
                    return true;
                })
                .orElse(false);
    }

    private ProjectDto.Res toRes(Project project) {
        return new ProjectDto.Res(
                project.getId(),
                project.getName(),
                project.getType(),
                project.getOwnerId()
        );
    }

}
