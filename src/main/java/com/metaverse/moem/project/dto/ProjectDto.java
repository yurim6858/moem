package com.metaverse.moem.project.dto;

import com.metaverse.moem.project.domain.Project;
import com.metaverse.moem.project.domain.ProjectType;
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
            String teamName
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
                    .recruitCurrent(project.getRecruitCurrent())
                    .recruitStartDate(project.getRecruitStartDate())
                    .recruitEndDate(project.getRecruitEndDate())
                    .projectStartDate(project.getProjectStartDate())
                    .projectEndDate(project.getProjectEndDate())
                    .teamId(project.getTeam() != null ? project.getTeam().getId() : null)
                    .ownerId(null) //
                    .build();
        }
    }

        public record SearchCondition(
                ProjectType type,
                String status,
                String query
        ) {}

}
