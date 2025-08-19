package com.metaverse.moem.assignment.personal_assignment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class PersonalAssignmentDto {

    // 개인 과제 생성 요청
    public record CreateReq(
            Long teamAssignmentId, // null 일 경우 유저가 생성 요청
            @NotNull Long userId,
            @NotBlank String title,
            String description,
            @NotNull LocalDateTime dueAt
    ) {}

    // 사용자가 직접 추가한 과제 수정
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
