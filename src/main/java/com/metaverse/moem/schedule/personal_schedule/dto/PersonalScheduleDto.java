package com.metaverse.moem.schedule.personal_schedule.dto;

import java.time.LocalDateTime;

public class PersonalScheduleDto {

    public enum AssignmentStatus {
        신규,
        여유,
        마감임박,
        마감지남,
        알수없음
    }

    public record Res(
            Long id,
            String title,
            String description,
            LocalDateTime dueAt,
            boolean userCreated,
            AssignmentStatus status
    ) {}
}
