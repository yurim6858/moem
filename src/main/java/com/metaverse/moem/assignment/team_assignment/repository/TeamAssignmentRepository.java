package com.metaverse.moem.assignment.team_assignment.repository;

import com.metaverse.moem.assignment.team_assignment.domain.TeamAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeamAssignmentRepository extends JpaRepository<TeamAssignment, Long> {
    List<TeamAssignment> findAllByProjectId(Long projectId);
    List<TeamAssignment> findAllByTeamId(Long teamId);
    List<TeamAssignment> findAllByProjectIdAndUserId(Long projectId, Long userId);
}
