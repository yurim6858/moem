package com.metaverse.moem.team.dto;

import com.metaverse.moem.team.domain.Role;
import com.metaverse.moem.team.domain.TeamMembers;
import lombok.Builder;

public class TeamMembersDto {

    public record CreateReq(Long userId) {}

    public record UpdateReq(Role role) {}

    @Builder
    public record Res(
            Long id,
            Long teamId,
            Long userId,
            Role role,
            String createdAt,
            String updatedAt
    ) {
        public static Res from(TeamMembers members) {
            return Res.builder()
                    .id(members.getId())
                    .teamId(members.getTeam().getId())
                    .userId(members.getUserId())
                    .role(members.getRole())
                    .createdAt(members.getCreatedAt() == null ? null : members.getCreatedAt().toString())
                    .updatedAt(members.getUpdatedAt() == null ? null : members.getUpdatedAt().toString())
                    .build();
        }
    }

}
