package com.metaverse.moem.team.dto;

import com.metaverse.moem.team.domain.Team;
import lombok.Builder;

public class TeamDto {

    public record CreateReq(String name, Integer maxMembers) {}
    public record UpdateReq(String name, Integer maxMembers) {}
    @Builder
    public record Res(Long id, String name, Integer maxMembers,
                      String createdAt, String updatedAt) {
        public static Res from(Team team) {
            return Res.builder()
                    .id(team.getId())
                    .name(team.getName())
                    .maxMembers(team.getMaxMembers())
                    .createdAt(team.getCreatedAt() == null ? null : team.getCreatedAt().toString())
                    .updatedAt(team.getUpdatedAt() == null ? null : team.getUpdatedAt().toString())
                    .build();
        }
    }
}
