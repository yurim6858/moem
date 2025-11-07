package com.metaverse.moem.team.dto;

import com.metaverse.moem.auth.repository.UserRepository;
import com.metaverse.moem.team.domain.TeamMemberLeaveRequest;
import lombok.Builder;
import jakarta.validation.constraints.Size;

public class TeamMemberLeaveRequestDto {

    // 나가기 요청 생성
    public record CreateReq(
            @Size(max = 500) String reason
    ) {}

    // 나가기 요청 응답
    @Builder
    public record Res(
            Long id,
            Long teamId,
            Long teamMemberId,
            Long userId,
            String username,
            String reason,
            String status,
            String createdAt,
            String respondedAt
    ) {
        public static Res from(TeamMemberLeaveRequest request, UserRepository userRepository) {
            if (request == null) {
                return Res.builder()
                        .id(0L)
                        .teamId(null)
                        .teamMemberId(null)
                        .userId(0L)
                        .username("")
                        .reason("")
                        .status("PENDING")
                        .createdAt("")
                        .respondedAt("")
                        .build();
            }

            Long userId = null;
            String username = "";
            try {
                if (request.getTeamMember() != null) {
                    userId = request.getTeamMember().getUserId();
                    if (userId != null && userRepository != null) {
                        var userOpt = userRepository.findById(userId);
                        if (userOpt.isPresent()) {
                            username = userOpt.get().getUsername() != null ? userOpt.get().getUsername() : "";
                        }
                    }
                }
            } catch (Exception e) {
                // 사용자 정보 조회 실패 시 기본값 유지
            }

            Long teamId = null;
            try {
                if (request.getTeam() != null) {
                    teamId = request.getTeam().getId();
                }
            } catch (Exception e) {
                // 팀 정보 조회 실패 시 null 유지
            }

            return Res.builder()
                    .id(request.getId() != null ? request.getId() : 0L)
                    .teamId(teamId)
                    .teamMemberId(request.getTeamMember() != null ? request.getTeamMember().getId() : null)
                    .userId(userId)
                    .username(username)
                    .reason(request.getReason() != null ? request.getReason() : "")
                    .status(request.getStatus() != null ? request.getStatus().name() : "PENDING")
                    .createdAt(request.getCreatedAt() != null ? request.getCreatedAt().toString() : "")
                    .respondedAt(request.getRespondedAt() != null ? request.getRespondedAt().toString() : "")
                    .build();
        }
    }

    // 나가기 요청 승인/거절
    public record RespondReq(
            String status  // "APPROVED" or "REJECTED"
    ) {}
}

