package com.metaverse.moem.schedule.team_schedule.service;

import com.metaverse.moem.assignment.team_assignment.domain.TeamAssignment;
import com.metaverse.moem.assignment.team_assignment.repository.TeamAssignmentRepository;
import com.metaverse.moem.schedule.team_schedule.dto.TeamScheduleDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TeamScheduleService {

    private final TeamAssignmentRepository teamAssignmentRepository;

    public List<TeamScheduleDto.Res> getSchedules(Long teamId) {
        LocalDateTime now = LocalDateTime.now();

        List<TeamAssignment> assignments = teamAssignmentRepository.findAllByProject_TeamId(teamId);

        return assignments.stream()
                .map(teamAssignment -> new TeamScheduleDto.Res(
                        teamAssignment.getId(),
                        teamAssignment.getTitle(),
                        teamAssignment.getDescription(),
                        teamAssignment.getDueAt(),
                        teamAssignment.getUserId(),
                        teamAssignment.getCreatedAt(),
                        calculateStatus(teamAssignment.getDueAt(), teamAssignment.getCreatedAt(), now)
                ))
                .toList();
    }

    private TeamScheduleDto.AssignmentStatus calculateStatus(LocalDateTime dueAt, LocalDateTime createdAt, LocalDateTime now) {
        if (dueAt == null || createdAt == null) {
            return TeamScheduleDto.AssignmentStatus.알수없음;
        }

        if (createdAt.toLocalDate().isEqual(now.toLocalDate())) {
            return TeamScheduleDto.AssignmentStatus.신규;
        }

        if (dueAt.isBefore(now)) {
            return TeamScheduleDto.AssignmentStatus.마감지남;
        }

        if (now.plusDays(3).isAfter(dueAt)) {
            return TeamScheduleDto.AssignmentStatus.마감임박;
        }

        return TeamScheduleDto.AssignmentStatus.여유;
    }


}
