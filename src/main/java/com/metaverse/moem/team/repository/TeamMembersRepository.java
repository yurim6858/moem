package com.metaverse.moem.team.repository;

import com.metaverse.moem.team.domain.TeamMembers;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TeamMembersRepository extends JpaRepository<TeamMembers, Long> {
    boolean existsByTeamIdAndUserId(Long teamId, Long userId);
    Optional<TeamMembers> findByTeamIdAndUserId(Long teamId, Long userId);
    List<TeamMembers> findByTeamId(Long teamId);
    List<TeamMembers> findByUserId(Long userId);
    long countByTeamId(Long teamId);
}
