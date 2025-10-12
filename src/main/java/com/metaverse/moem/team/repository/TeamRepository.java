package com.metaverse.moem.team.repository;

import com.metaverse.moem.team.domain.Team;
import org.springframework.data.jpa.repository.JpaRepository;

// JPARepository를 상속받으면 기본적인 CRUD 메서드 자동 제공
public interface TeamRepository extends JpaRepository<Team, Long> {

    // 팀 이름이 이미 존재하는지 확인하는 쿼리 메서드
    boolean existsByName(String name);
}
