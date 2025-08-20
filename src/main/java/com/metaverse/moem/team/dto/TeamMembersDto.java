package com.metaverse.moem.team.dto;

import jakarta.validation.constraints.NotNull;

public class TeamMembersDto {

    public record CreateReq(
            @NotNull Long teamId,
            @NotNull Long userId,
            String role) {
    }

    public record UpdateReq(
            String name,
            String role) {
    }

    public record DeleteReq(
            @NotNull Long id) {
    }

    public record Res(
            Long id,
            String name,
            String role,
            Long teamId,
            String CreatedAt,
            String UpdatedAt) {
    }

}
