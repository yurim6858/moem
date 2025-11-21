package com.metaverse.moem.assignment.team_assignment.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.metaverse.moem.assignment.team_assignment.domain.TeamAssignment;
import com.metaverse.moem.assignment.team_assignment.domain.WeeklyReport;
import com.metaverse.moem.assignment.team_assignment.dto.WeeklyReportDto;
import com.metaverse.moem.assignment.team_assignment.repository.TeamAssignmentRepository;
import com.metaverse.moem.assignment.team_assignment.repository.WeeklyReportRepository;
import com.metaverse.moem.matching.service.GeminiService;
import com.metaverse.moem.project.domain.Project;
import com.metaverse.moem.project.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class WeeklyReportService {

    private final WeeklyReportRepository weeklyReportRepository;
    private final TeamAssignmentRepository teamAssignmentRepository;
    private final ProjectRepository projectRepository;
    
    @Autowired(required = false)
    private GeminiService geminiService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 매주 월요일 오전 9시에 주간 리포트 생성
     */
    @Scheduled(cron = "0 0 9 * * MON") // 매주 월요일 오전 9시
    @Transactional
    public void generateWeeklyReports() {
        log.info("주간 리포트 생성 시작");

        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(DayOfWeek.MONDAY).minusWeeks(1); // 지난 주 월요일
        LocalDate weekEnd = weekStart.plusDays(6); // 지난 주 일요일

        List<Project> activeProjects = projectRepository.findActiveProjects();

        for (Project project : activeProjects) {
            try {
                // 이미 리포트가 생성되었는지 확인
                Optional<WeeklyReport> existing = weeklyReportRepository
                        .findByProjectIdAndWeekStartDate(project.getId(), weekStart);

                if (existing.isPresent()) {
                    log.info("프로젝트 {}의 {}주차 리포트가 이미 존재합니다.", project.getId(), weekStart);
                    continue;
                }

                WeeklyReportDto report = generateReport(project, weekStart, weekEnd);
                saveReport(project, weekStart, weekEnd, report);
                log.info("프로젝트 {}의 {}주차 리포트 생성 완료", project.getId(), weekStart);
            } catch (Exception e) {
                log.error("프로젝트 {}의 주간 리포트 생성 실패", project.getId(), e);
            }
        }

        log.info("주간 리포트 생성 완료");
    }

    /**
     * 특정 프로젝트의 주간 리포트 생성
     */
    @Transactional
    public WeeklyReportDto generateReport(Project project, LocalDate weekStart, LocalDate weekEnd) throws IOException {
        // 1. 주간 데이터 수집
        WeeklyData data = collectWeeklyData(project, weekStart, weekEnd);

        // 2. AI에게 평가 요청
        String aiAnalysis = generateAIAnalysis(project, data, weekStart, weekEnd);

        // 3. 리포트 생성
        return WeeklyReportDto.builder()
                .projectId(project.getId())
                .weekStartDate(weekStart)
                .weekEndDate(weekEnd)
                .performanceData(data.performance)
                .collaborationData(data.collaboration)
                .attitudeData(data.attitude)
                .aiAnalysis(aiAnalysis)
                .build();
    }

    private WeeklyData collectWeeklyData(Project project, LocalDate weekStart, LocalDate weekEnd) {
        List<TeamAssignment> assignments = teamAssignmentRepository.findAllByProjectId(project.getId());

        // 주차 기간 내의 과제들 필터링
        List<TeamAssignment> weekAssignments = assignments.stream()
                .filter(a -> {
                    LocalDate dueDate = a.getDueAt().toLocalDate();
                    return !dueDate.isBefore(weekStart) && !dueDate.isAfter(weekEnd);
                })
                .toList();

        // 성과 지표 계산
        Map<String, Object> performance = calculatePerformanceMetrics(weekAssignments, assignments);

        // 협업 지표 계산 (현재는 기본값, 추후 확장)
        Map<String, Object> collaboration = new HashMap<>();
        collaboration.put("meetingCount", 0);
        collaboration.put("communicationFrequency", "medium");

        // 태도 지표 계산 (현재는 기본값, 추후 확장)
        Map<String, Object> attitude = new HashMap<>();
        attitude.put("averageResponseTime", "N/A");
        attitude.put("proactiveSuggestions", 0);

        return new WeeklyData(performance, collaboration, attitude);
    }

    private Map<String, Object> calculatePerformanceMetrics(
            List<TeamAssignment> weekAssignments, 
            List<TeamAssignment> allAssignments) {
        Map<String, Object> metrics = new HashMap<>();

        int totalAssignments = weekAssignments.size();
        long completedAssignments = weekAssignments.stream()
                .filter(a -> a.getStatus() == TeamAssignment.AssignmentStatus.COMPLETED)
                .count();
        long delayedAssignments = weekAssignments.stream()
                .filter(a -> a.getStatus() == TeamAssignment.AssignmentStatus.DELAYED)
                .count();

        double completionRate = totalAssignments > 0 
                ? (double) completedAssignments / totalAssignments * 100 
                : 0;

        metrics.put("totalAssignments", totalAssignments);
        metrics.put("completedAssignments", completedAssignments);
        metrics.put("inProgressAssignments", weekAssignments.stream()
                .filter(a -> a.getStatus() == TeamAssignment.AssignmentStatus.IN_PROGRESS)
                .count());
        metrics.put("delayedAssignments", delayedAssignments);
        metrics.put("completionRate", Math.round(completionRate * 100.0) / 100.0);

        // 전체 프로젝트 진행률
        long allCompleted = allAssignments.stream()
                .filter(a -> a.getStatus() == TeamAssignment.AssignmentStatus.COMPLETED)
                .count();
        double overallProgress = allAssignments.size() > 0 
                ? (double) allCompleted / allAssignments.size() * 100 
                : 0;
        metrics.put("overallProgress", Math.round(overallProgress * 100.0) / 100.0);

        return metrics;
    }

    private String generateAIAnalysis(Project project, WeeklyData data, LocalDate weekStart, LocalDate weekEnd) throws IOException {
        if (geminiService == null) {
            log.warn("GeminiService가 사용 불가능합니다. 기본 리포트를 반환합니다.");
            return String.format("""
                프로젝트: %s
                기간: %s ~ %s
                
                성과 지표:
                - 전체 과제: %d개
                - 완료된 과제: %d개
                - 진행 중인 과제: %d개
                - 지연된 과제: %d개
                - 완료율: %.2f%%
                - 전체 진행률: %.2f%%
                
                (AI 분석 기능을 사용하려면 Gemini API 키를 설정해주세요.)
                """, project.getName(), weekStart, weekEnd,
                data.performance.get("totalAssignments"),
                data.performance.get("completedAssignments"),
                data.performance.get("inProgressAssignments"),
                data.performance.get("delayedAssignments"),
                data.performance.get("completionRate"),
                data.performance.get("overallProgress"));
        }
        
        String prompt = String.format("""
            다음 주간 데이터를 바탕으로 평가 리포트를 작성해주세요:
            
            프로젝트: %s
            기간: %s ~ %s
            
            성과 지표:
            - 전체 과제: %d개
            - 완료된 과제: %d개
            - 진행 중인 과제: %d개
            - 지연된 과제: %d개
            - 완료율: %.2f%%
            - 전체 진행률: %.2f%%
            
            다음 형식으로 평가해주세요:
            1. 전체 진행률 평가
            2. 주요 성과 및 문제점
            3. 다음 주 권장사항
            
            간결하고 명확하게 작성해주세요.
            """, project.getName(), weekStart, weekEnd,
            data.performance.get("totalAssignments"),
            data.performance.get("completedAssignments"),
            data.performance.get("inProgressAssignments"),
            data.performance.get("delayedAssignments"),
            data.performance.get("completionRate"),
            data.performance.get("overallProgress"));

        return geminiService.getCompletion(prompt);
    }

    private void saveReport(Project project, LocalDate weekStart, LocalDate weekEnd, WeeklyReportDto report) {
        try {
            // 주차 번호 계산
            LocalDate projectStart = project.getProjectStartDate();
            int weekNumber = (int) java.time.temporal.ChronoUnit.WEEKS.between(projectStart, weekStart) + 1;

            WeeklyReport weeklyReport = WeeklyReport.builder()
                    .project(project)
                    .weekStartDate(weekStart)
                    .weekEndDate(weekEnd)
                    .weekNumber(weekNumber)
                    .performanceData(objectMapper.writeValueAsString(report.performanceData()))
                    .collaborationData(objectMapper.writeValueAsString(report.collaborationData()))
                    .attitudeData(objectMapper.writeValueAsString(report.attitudeData()))
                    .aiAnalysis(report.aiAnalysis())
                    .build();

            weeklyReportRepository.save(weeklyReport);
        } catch (Exception e) {
            log.error("주간 리포트 저장 실패", e);
            throw new RuntimeException("주간 리포트 저장 실패", e);
        }
    }

    /**
     * 프로젝트의 주간 리포트 목록 조회
     */
    @Transactional(readOnly = true)
    public List<WeeklyReportDto.Res> getReportsByProject(Long projectId) {
        return weeklyReportRepository.findByProjectIdOrderByWeekStartDateDesc(projectId).stream()
                .map(this::toRes)
                .toList();
    }

    /**
     * 특정 주차 리포트 조회
     */
    @Transactional(readOnly = true)
    public Optional<WeeklyReportDto.Res> getReportByWeek(Long projectId, LocalDate weekStartDate) {
        return weeklyReportRepository.findByProjectIdAndWeekStartDate(projectId, weekStartDate)
                .map(this::toRes);
    }

    /**
     * 수동으로 주간 리포트 생성
     */
    @Transactional
    public WeeklyReportDto.Res generateReportManually(WeeklyReportDto.CreateReq req) throws IOException {
        Project project = projectRepository.findById(req.projectId())
                .orElseThrow(() -> new IllegalArgumentException("프로젝트가 존재하지 않습니다."));

        WeeklyReportDto report = generateReport(project, req.weekStartDate(), req.weekEndDate());
        saveReport(project, req.weekStartDate(), req.weekEndDate(), report);

        return toRes(weeklyReportRepository.findByProjectIdAndWeekStartDate(
                project.getId(), req.weekStartDate())
                .orElseThrow(() -> new RuntimeException("리포트 저장 실패")));
    }

    private WeeklyReportDto.Res toRes(com.metaverse.moem.assignment.team_assignment.domain.WeeklyReport weeklyReport) {
        try {
            Map<String, Object> performanceData = objectMapper.readValue(
                    weeklyReport.getPerformanceData(),
                    new TypeReference<Map<String, Object>>() {}
            );
            Map<String, Object> collaborationData = objectMapper.readValue(
                    weeklyReport.getCollaborationData(),
                    new TypeReference<Map<String, Object>>() {}
            );
            Map<String, Object> attitudeData = objectMapper.readValue(
                    weeklyReport.getAttitudeData(),
                    new TypeReference<Map<String, Object>>() {}
            );

            return new WeeklyReportDto.Res(
                    weeklyReport.getId(),
                    weeklyReport.getProject().getId(),
                    weeklyReport.getWeekStartDate(),
                    weeklyReport.getWeekEndDate(),
                    weeklyReport.getWeekNumber(),
                    performanceData,
                    collaborationData,
                    attitudeData,
                    weeklyReport.getAiAnalysis()
            );
        } catch (Exception e) {
            log.error("리포트 변환 실패", e);
            throw new RuntimeException("리포트 변환 실패", e);
        }
    }

    private static class WeeklyData {
        Map<String, Object> performance;
        Map<String, Object> collaboration;
        Map<String, Object> attitude;

        WeeklyData(Map<String, Object> performance, Map<String, Object> collaboration, Map<String, Object> attitude) {
            this.performance = performance;
            this.collaboration = collaboration;
            this.attitude = attitude;
        }
    }
}

