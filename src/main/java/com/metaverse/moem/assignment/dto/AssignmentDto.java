package com.metaverse.moem.assignment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class AssignmentDto {

    public record CreateReq(
            @NotNull Long projectId,
            @NotNull Long userId,
            @NotBlank String title,
            String description,
            @NotNull LocalDateTime dueAt
    ) {}

    public record Res(
            Long id,
            Long projectId,
            Long userId,
            String title,
            String description,
            LocalDateTime dueAt
    ) {}
}
