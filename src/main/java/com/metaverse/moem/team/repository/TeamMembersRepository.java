package com.metaverse.moem.team.repository;

import com.metaverse.moem.team.domain.TeamMembers;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeamMembersRepository extends JpaRepository<TeamMembers, Long> {

    List<TeamMembers> findByTeamId(Long teamId);
}
