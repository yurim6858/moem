package com.metaverse.moem.team.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;

public class TeamDto {

    public record CreateReq(
            @NotBlank @Size(max = 60) String name,
            @Size(max = 255) String description) {
    }

    public record UpdateReq(
            @NotBlank @Size(max = 60) String name,
            @Size(max = 255) String description) {
    }

    public record DeleteReq(
            @NotNull Long id) {
    }

    public record Res(
            Long id,
            String name,
            String description,
            String CreatedAt,
            String UpdatedAt) {
    }
}
