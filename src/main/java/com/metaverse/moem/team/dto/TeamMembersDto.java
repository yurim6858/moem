package com.metaverse.moem.team.dto;

import jakarta.validation.constraints.NotNull;

public class TeamMembersDto {

    // 팀원 생성 요청
    public record CreateReq(
            @NotNull Long userId,
            String role, // 역할 정보
            String name // 사용자 이름
    ) {}

    // 팀원 수정 요청
    public record UpdateReq(
            String name,
            String role
    ) {}

    // 팀원 삭제 요청
    public record  DeleteReq(
            @NotNull Long id
    ) {}

    // 팀원 응답 (API 결과 반환시 사용)
    public record Res(Long id, String name, String role, Long teamId, Long userId,
                      String CreatedAt, String UpdatedAt) {}

}
