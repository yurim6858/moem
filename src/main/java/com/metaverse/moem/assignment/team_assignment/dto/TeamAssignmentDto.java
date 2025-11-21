package com.metaverse.moem.assignment.team_assignment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class TeamAssignmentDto {

    public record CreateReq(
            @NotNull Long projectId,
            @NotNull Long userId,
            @NotBlank String title,
            String description,
            @NotNull LocalDateTime dueAt
    ) {}

    public record UpdateReq(
            String title,
            String description,
            LocalDateTime dueAt
    ) {}

    public record Res(
            Long id,
            Long projectId,
            Long userId,
            String title,
            String description,
            LocalDateTime dueAt,
            String status,
            Integer progress,
            LocalDateTime completedAt,
            Integer delayDays,
            String completionNotes
    ) {}

    public record UpdateStatusReq(
            String status,
            Integer progress,
            String completionNotes
    ) {}
}
