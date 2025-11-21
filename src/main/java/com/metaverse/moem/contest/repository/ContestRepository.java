package com.metaverse.moem.contest.repository;

import com.metaverse.moem.contest.domain.Contest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ContestRepository extends JpaRepository<Contest, Long> {
    Optional<Contest> findByTitleAndHostAndDeadline(String title, String host, LocalDate deadline);
    List<Contest> findAllByDeadlineAfterOrderByCreatedAtDesc(LocalDate date);
}