package com.metaverse.moem.team.repository;

import com.metaverse.moem.team.domain.Team;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TeamRepository extends JpaRepository<Team, Long> {

    Optional<Team> findById(Long id);
    Optional<Team> findByProjectId(Long projectId);
    boolean existsByName(String name);
}
