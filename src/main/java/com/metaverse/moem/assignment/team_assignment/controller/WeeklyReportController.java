package com.metaverse.moem.assignment.team_assignment.controller;

import com.metaverse.moem.assignment.team_assignment.dto.WeeklyReportDto;
import com.metaverse.moem.assignment.team_assignment.service.WeeklyReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/weekly-reports")
@RequiredArgsConstructor
@ConditionalOnBean(WeeklyReportService.class)
public class WeeklyReportController {

    private final WeeklyReportService weeklyReportService;

    /**
     * 프로젝트의 주간 리포트 목록 조회
     */
    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<WeeklyReportDto.Res>> getReportsByProject(@PathVariable Long projectId) {
        try {
            List<WeeklyReportDto.Res> reports = weeklyReportService.getReportsByProject(projectId);
            return ResponseEntity.ok(reports);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 특정 주차 리포트 조회
     */
    @GetMapping("/project/{projectId}/week/{weekStartDate}")
    public ResponseEntity<WeeklyReportDto.Res> getReportByWeek(
            @PathVariable Long projectId,
            @PathVariable LocalDate weekStartDate) {
        try {
            Optional<WeeklyReportDto.Res> report = weeklyReportService.getReportByWeek(projectId, weekStartDate);
            return report.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 수동으로 주간 리포트 생성
     */
    @PostMapping("/generate")
    public ResponseEntity<WeeklyReportDto.Res> generateReport(@RequestBody WeeklyReportDto.CreateReq req) {
        try {
            WeeklyReportDto.Res report = weeklyReportService.generateReportManually(req);
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}

