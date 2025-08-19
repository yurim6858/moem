package com.metaverse.moem.project.dto;

import com.metaverse.moem.project.domain.ProjectType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ProjectDto {

    public record CreateReq(
            @NotBlank String name,
            @NotNull ProjectType type,
            @NotNull Long ownerId
    ) {}

    public record UpdateReq(
            @NotBlank String name
    ) {}

    public record Res(
            Long id,
            String name,
            ProjectType type,
            Long ownerId
    ) {}
}
