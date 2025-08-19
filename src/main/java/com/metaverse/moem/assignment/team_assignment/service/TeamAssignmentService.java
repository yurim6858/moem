package com.metaverse.moem.assignment.team_assignment.service;

import com.metaverse.moem.assignment.team_assignment.domain.TeamAssignment;
import com.metaverse.moem.assignment.team_assignment.dto.TeamAssignmentDto;
import com.metaverse.moem.assignment.team_assignment.repository.TeamAssignmentRepository;
import com.metaverse.moem.team.domain.Team;
import lombok.RequiredArgsConstructor;
import org.springframework.expression.spel.ast.Assign;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TeamAssignmentService {

    private final TeamAssignmentRepository teamAssignmentRepository;

    public TeamAssignmentDto.Res create(TeamAssignmentDto.CreateReq req) {
        TeamAssignment teamAssignment =  TeamAssignment.builder()
                    .projectId(req.projectId())
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
                a.getProjectId(),
                a.getUserId(),
                a.getTitle(),
                a.getDescription(),
                a.getDueAt()
        );
    }
}
