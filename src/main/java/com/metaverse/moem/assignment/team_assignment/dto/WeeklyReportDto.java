package com.metaverse.moem.assignment.team_assignment.dto;

import java.time.LocalDate;
import java.util.Map;

public class WeeklyReportDto {

    public record Res(
            Long id,
            Long projectId,
            LocalDate weekStartDate,
            LocalDate weekEndDate,
            Integer weekNumber,
            Map<String, Object> performanceData,
            Map<String, Object> collaborationData,
            Map<String, Object> attitudeData,
            String aiAnalysis
    ) {}

    public record CreateReq(
            Long projectId,
            LocalDate weekStartDate,
            LocalDate weekEndDate
    ) {}

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long projectId;
        private LocalDate weekStartDate;
        private LocalDate weekEndDate;
        private Map<String, Object> performanceData;
        private Map<String, Object> collaborationData;
        private Map<String, Object> attitudeData;
        private String aiAnalysis;

        public Builder projectId(Long projectId) {
            this.projectId = projectId;
            return this;
        }

        public Builder weekStartDate(LocalDate weekStartDate) {
            this.weekStartDate = weekStartDate;
            return this;
        }

        public Builder weekEndDate(LocalDate weekEndDate) {
            this.weekEndDate = weekEndDate;
            return this;
        }

        public Builder performanceData(Map<String, Object> performanceData) {
            this.performanceData = performanceData;
            return this;
        }

        public Builder collaborationData(Map<String, Object> collaborationData) {
            this.collaborationData = collaborationData;
            return this;
        }

        public Builder attitudeData(Map<String, Object> attitudeData) {
            this.attitudeData = attitudeData;
            return this;
        }

        public Builder aiAnalysis(String aiAnalysis) {
            this.aiAnalysis = aiAnalysis;
            return this;
        }

        public WeeklyReportDto build() {
            return new WeeklyReportDto(projectId, weekStartDate, weekEndDate,
                    performanceData, collaborationData, attitudeData, aiAnalysis);
        }
    }

    private final Long projectId;
    private final LocalDate weekStartDate;
    private final LocalDate weekEndDate;
    private final Map<String, Object> performanceData;
    private final Map<String, Object> collaborationData;
    private final Map<String, Object> attitudeData;
    private final String aiAnalysis;

    private WeeklyReportDto(Long projectId, LocalDate weekStartDate, LocalDate weekEndDate,
                           Map<String, Object> performanceData, Map<String, Object> collaborationData,
                           Map<String, Object> attitudeData, String aiAnalysis) {
        this.projectId = projectId;
        this.weekStartDate = weekStartDate;
        this.weekEndDate = weekEndDate;
        this.performanceData = performanceData;
        this.collaborationData = collaborationData;
        this.attitudeData = attitudeData;
        this.aiAnalysis = aiAnalysis;
    }

    public Long projectId() { return projectId; }
    public LocalDate weekStartDate() { return weekStartDate; }
    public LocalDate weekEndDate() { return weekEndDate; }
    public Map<String, Object> performanceData() { return performanceData; }
    public Map<String, Object> collaborationData() { return collaborationData; }
    public Map<String, Object> attitudeData() { return attitudeData; }
    public String aiAnalysis() { return aiAnalysis; }
}

