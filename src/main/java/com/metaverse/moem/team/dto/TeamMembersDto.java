package com.metaverse.moem.team.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class TeamMembersDto {

    // 팀원 생성 요청
    public record CreateReq(
            @NotNull Long teamId, // 소속 team Id
            @NotBlank String name,
            String role // 역할 정보
    ) {}

    // 팀원 수정 요청
    public record  UpdateReq(
            @NotNull Long id, // 수정대성 member의 id
            String name,
            String role
    ) {}

    // 팀원 삭제 요청
    public record  DeleteReq(
            @NotNull Long id
    ) {}

    // 팀원 응답 (API 결과 반환시 사용)
    public record Res(Long id, String name, String role, Long teamId,
                      String CreatedAt, String UpdatedAt) {}

}
