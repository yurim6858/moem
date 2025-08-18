package com.metaverse.moem.team.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.NotNull;

public class TeamDto {

    // 팀 생성 요청
    public record CreateReq(
            @NotBlank @Size(max = 60) String name, // 이름 필수 최대 60자
            @Size(max = 255) String description //  설명 선택 최대 255자
    ) {}

    // 팀 수정 요청
    public record  UpdateReq(
            @NotBlank @Size(max = 60) String name,
            @Size(max = 255) String description
    ) {}

    // 팀 삭제 요청
    public record  DeleteReq(
            @NotNull Long id
    ) {}

    // 팀 응답 (API 결과 반환시 사용)
    public record Res(Long id, String name, String description,
                      String CreatedAt, String UpdatedAt) {}
}
