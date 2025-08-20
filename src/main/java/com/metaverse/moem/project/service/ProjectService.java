package com.metaverse.moem.project.service;

import com.metaverse.moem.project.domain.Project;
import com.metaverse.moem.project.domain.ProjectType;
import com.metaverse.moem.project.dto.ProjectDto;
import com.metaverse.moem.project.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;

    // 생성
    public ProjectDto.Res create(ProjectDto.CreateReq req) {
        Project project = Project.builder()
                .name(req.name())
                .type(req.type())
                .ownerId(req.ownerId())
                .createdAt(java.time.LocalDateTime.now())
                .isDeleted(false)
                .build();

        Project saved = projectRepository.save(project);
        return toRes(saved);
    }

    // 단일 조회
    public Optional<ProjectDto.Res> get(Long id) {
        return projectRepository.findById(id)
                .filter(p -> !p.isDeleted())
                .map(this::toRes);
    }

    // 소유자별 조회
    public List<ProjectDto.Res> listByOwner(ProjectType type, Long ownerId) {
        return projectRepository.findByTypeAndOwnerIdAndIsDeletedFalse(type, ownerId)
                .stream()
                .map(this::toRes)
                .toList();
    }


    // 수정
    public Optional<ProjectDto.Res> update(Long id,ProjectDto.UpdateReq req) {
        return projectRepository.findById(id)
                .filter(p -> !p.isDeleted())
                .map(project -> {
                    project.setName( req.name());
                    project.setUpdatedAt(LocalDateTime.now());
                    return toRes(projectRepository.save(project));
                });
    }

    // 삭제
    public boolean delete(Long id) {
        return projectRepository.findById(id)
                .filter(p -> !p.isDeleted())
                .map(project -> {
                    project.setDeleted(true);
                    project.setUpdatedAt(LocalDateTime.now());
                    projectRepository.save(project);
                    return true;
                })
                .orElse(false);
    }

    private ProjectDto.Res toRes(Project p) {
        return new ProjectDto.Res(
                p.getId(),
                p.getName(),
                p.getType(),
                p.getOwnerId()
        );
    }


}
