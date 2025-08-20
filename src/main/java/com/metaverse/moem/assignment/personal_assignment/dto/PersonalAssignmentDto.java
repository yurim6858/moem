package com.metaverse.moem.assignment.personal_assignment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class PersonalAssignmentDto {

    public record CreateFromTeamReq(
            @NotNull Long userId,
            @NotNull Long teamAssignmentId
    ) {}

    public record CreateOwnReq(
            @NotNull Long userId,
            @NotBlank String title,
            String description,
            @NotNull LocalDateTime dueAt
    ) {}


    public record UpdateReq(
            @NotBlank String title,
            String description,
            @NotNull LocalDateTime dueAt
    ) {}

    public record Res(
            Long id,
            Long teamAssignmentid,
            Long userId,
            String title,
            String description,
            LocalDateTime dueAt,
            boolean userCreated
    ) {}
}
