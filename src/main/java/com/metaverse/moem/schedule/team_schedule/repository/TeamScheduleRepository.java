package com.metaverse.moem.schedule.team_schedule.repository;

import com.metaverse.moem.assignment.team_assignment.domain.TeamAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeamScheduleRepository extends JpaRepository<TeamAssignment, Long> {

    List<TeamAssignment> findAllByProject_TeamId(Long teamId);
}
