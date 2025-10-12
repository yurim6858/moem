package com.metaverse.moem.assignment.team_assignment.service;

import com.metaverse.moem.assignment.team_assignment.domain.TeamAssignment;
import com.metaverse.moem.assignment.team_assignment.dto.TeamAssignmentDto;
import com.metaverse.moem.assignment.team_assignment.repository.TeamAssignmentRepository;
import com.metaverse.moem.project.domain.Project;
import com.metaverse.moem.project.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TeamAssignmentService {

    private final TeamAssignmentRepository teamAssignmentRepository;
    private final ProjectRepository projectRepository;

    public TeamAssignmentDto.Res create(TeamAssignmentDto.CreateReq req) {
        Project project = projectRepository.findById(req.projectId())
                .orElseThrow(() -> new IllegalArgumentException("프로젝트가 존재하지 않습니다."));

        TeamAssignment teamAssignment = TeamAssignment.builder()
                .project(project)
                .userId(req.userId())
                .description(req.description())
                .title(req.title())
                .dueAt(req.dueAt())
                .build();

        return toRes(teamAssignmentRepository.save(teamAssignment));
    }


    public List<TeamAssignmentDto.Res> getByProject(Long projectId) {
        return teamAssignmentRepository.findAllByProjectId(projectId).stream()
                .map(this::toRes)
                .toList();
    }

    public List<TeamAssignmentDto.Res> getByProjectAndUser(Long projectId, Long userId) {
        return teamAssignmentRepository.findAllByProjectIdAndUserId(projectId, userId).stream()
                .map(this::toRes)
                .toList();
    }

    public TeamAssignmentDto.Res update(Long teamAssignmentId, TeamAssignmentDto.UpdateReq req) {
        TeamAssignment teamAssignment = teamAssignmentRepository.findById(teamAssignmentId)
                .orElseThrow(() -> new IllegalArgumentException("과제가 존재하지 않습니다."));

        teamAssignment.update(req.title(), req.description(), req.dueAt());
        return  toRes(teamAssignment);
    }

    public void delete(Long teamAssignmentId) {
        teamAssignmentRepository.deleteById(teamAssignmentId);
    }

    private TeamAssignmentDto.Res toRes(TeamAssignment a) {
        return new TeamAssignmentDto.Res(
                a.getId(),
                a.getProject().getId(),
                a.getUserId(),
                a.getTitle(),
                a.getDescription(),
                a.getDueAt()
        );
    }
}
