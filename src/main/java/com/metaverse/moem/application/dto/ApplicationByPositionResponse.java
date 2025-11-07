package com.metaverse.moem.application.dto;

import lombok.Builder;
import java.util.List;

public class ApplicationByPositionResponse {
    
    // 포지션별 지원자 그룹화 응답
    @Builder
    public record PositionApplications(
            String position,           // 포지션명
            int required,              // 필요 인원
            int current,               // 현재 승인된 인원
            boolean isRecruitmentCompleted,  // 모집완료 여부
            List<ApplicationResponse> applications  // 해당 포지션 지원자 목록
    ) {}
    
    // 전체 응답
    @Builder
    public record Res(
            Long projectId,
            List<PositionApplications> positions,
            boolean allPositionsCompleted,  // 모든 포지션 모집완료 여부
            boolean canStartProject         // 프로젝트 시작 가능 여부
    ) {}
}

