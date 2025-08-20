package com.metaverse.moem.schedule.team_schedule.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

public class TeamScheduleDto {

    @Getter
    @AllArgsConstructor
    public static class Res {
        private Long id;
        private String title;
        private String description;
        private LocalDateTime dueAt;
        private Long assignedUserId;
        private LocalDateTime createdAt;
        private AssignmentStatus status;
    }

    public enum AssignmentStatus {
        신규,
        여유,
        마감임박,
        마감지남,
        알수없음
    }
}
