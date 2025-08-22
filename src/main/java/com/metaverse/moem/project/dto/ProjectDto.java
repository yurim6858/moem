package com.metaverse.moem.project.dto;

import com.metaverse.moem.project.domain.Project;
import com.metaverse.moem.project.domain.ProjectType;
import jakarta.persistence.Basic;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.time.LocalDate;

public class ProjectDto {

    @Builder
    public record CreateReq(
            @NotBlank String name,
            @Size(max = 1000) String description,
            @NotNull ProjectType type,
            @PositiveOrZero Integer recruitTotal,
            LocalDate recruitStartDate,
            LocalDate recruitEndDate,
            LocalDate projectStartDate,
            LocalDate projectEndDate,
            @NotNull Long ownerId
    ) {}

    @Builder
    public record UpdateReq(
            @NotBlank String name,
            @Size(max = 1000) String description,
            @NotNull ProjectType type,
            @PositiveOrZero Integer recruitTotal,
            LocalDate recruitStartDate,
            LocalDate recruitEndDate,
            LocalDate projectStartDate,
            LocalDate projectEndDate
    ) {}

    @Builder
    public record Res(
            Long id,
            String name,
            String description,
            ProjectType type,
            Integer recruitTotal,
            Integer recruitCurrent,
            LocalDate recruitStartDate,
            LocalDate recruitEndDate,
            LocalDate projectStartDate,
            LocalDate projectEndDate,
            Long teamId,
            Long ownerId
    ) {
        public static Res from(Project project) {
            return Res.builder()
                    .id(project.getId())
                    .name(project.getName())
                    .description(project.getDescription())
                    .type(project.getType())
                    .recruitTotal(project.getRecruitTotal())
                    .recruitStartDate(project.getRecruitStartDate())
                    .recruitCurrent(project.getRecruitCurrent())
                    .recruitEndDate(project.getRecruitEndDate())
                    .projectStartDate(project.getProjectStartDate())
                    .projectEndDate(project.getProjectEndDate())
                    .teamId(project.getTeam() != null ? project.getTeam().getId() : null)
                    .ownerId(project.getTeam() != null ? project.getTeam().getOwner().getId() : null)
                    .build();
            }
        }

        public record SearchCondition(
                ProjectType type,
                String status,
                String query
        ) {}

}
