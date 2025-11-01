package com.metaverse.moem.team.dto;

import com.metaverse.moem.team.domain.Team;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

public class TeamDto {

    // 팀 생성 요청
    public record CreateReq(
            @NotBlank @Size(max = 60) String name,
            Integer maxMembers
    ) {}

    // 팀 수정 요청
    public record UpdateReq(
            @NotBlank @Size(max = 60) String name,
            Integer maxMembers
    ) {}

    // 팀 삭제 요청
    public record DeleteReq(
            @NotNull Long id
    ) {}

    // 팀 응답 (API 결과 반환시 사용)
    @Builder
    public record Res(
            Long id,
            String name,
            Integer maxMembers,
            String createdAt,
            String updatedAt
    ) {
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

    // 팀 상세 정보 응답 (멤버 포함)
    public record DetailRes(
            Long id,
            String name,
            Integer maxMembers,
            String createdAt,
            String updatedAt,
            java.util.List<TeamMembersDto.Res> members,
            int totalMembers,
            Long projectId
    ) {}

    // 프로젝트 시작 준비 상태 응답
    public record StartReadyRes(
            boolean isReadyToStart,
            int totalRequiredPositions,
            int filledPositions,
            double completionRate,
            java.util.List<PositionStatus> positionStatuses,
            String message
    ) {}

    // 포지션 상태 정보
    public record PositionStatus(
            String role,
            int required,
            int current,
            boolean isFilled
    ) {}

    // 프로젝트 시작 요청
    public record StartProjectReq(
            @NotNull Long projectId
    ) {}
}
