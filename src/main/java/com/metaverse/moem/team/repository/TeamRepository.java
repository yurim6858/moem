package com.metaverse.moem.team.repository;

import com.metaverse.moem.team.domain.Team;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<Team, Long> {

    boolean existsByName(String name);
}
