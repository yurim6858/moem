package com.metaverse.moem.assignment.team_assignment.repository;

import com.metaverse.moem.assignment.team_assignment.domain.WeeklyReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface WeeklyReportRepository extends JpaRepository<WeeklyReport, Long> {

    List<WeeklyReport> findByProjectIdOrderByWeekStartDateDesc(Long projectId);

    Optional<WeeklyReport> findByProjectIdAndWeekStartDate(Long projectId, LocalDate weekStartDate);

    @Query("""
        select wr from WeeklyReport wr
        where wr.project.id = :projectId
          and wr.weekStartDate <= :date
          and wr.weekEndDate >= :date
    """)
    Optional<WeeklyReport> findByProjectIdAndDate(@Param("projectId") Long projectId, @Param("date") LocalDate date);
}

