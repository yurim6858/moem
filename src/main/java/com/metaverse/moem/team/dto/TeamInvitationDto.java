package com.metaverse.moem.team.dto;

import com.metaverse.moem.team.domain.TeamInvitation;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class TeamInvitationDto {

    // 초대 발송 요청
    public record CreateReq(
            @NotNull(message = "초대받을 사용자 ID는 필수입니다.")
            Long invitedUserId,
            
            @Size(max = 500, message = "초대 메시지는 500자를 초과할 수 없습니다.")
            String message
    ) {}

    // 초대 응답 요청
    public record RespondReq(
            @NotBlank(message = "응답 상태는 필수입니다.")
            String status
    ) {}

    // 초대 응답
    public record Res(
            Long id,
            Long teamId,
            String teamName,
            String invitedUserUsername,
            String inviterUsername,
            String message,
            String status,
            String sentAt,
            String respondedAt
    ) {}

    // 초대 목록 조회용 (간단한 정보)
    public record ListRes(
            Long id,
            String teamName,
            String inviterUsername,
            String message,
            String status,
            String sentAt
    ) {}

    // Entity를 Response로 변환하는 메서드
    public static Res toRes(TeamInvitation invitation) {
        return new Res(
                invitation.getId(),
                invitation.getTeamId(),
                invitation.getTeamName(),
                invitation.getInvitedUserUsername(),
                invitation.getInviterUsername(),
                invitation.getMessage(),
                invitation.getStatus().name(),
                invitation.getSentAt() != null ? invitation.getSentAt().toString() : null,
                invitation.getRespondedAt() != null ? invitation.getRespondedAt().toString() : null
        );
    }

    // Entity를 List Response로 변환하는 메서드
    public static ListRes toListRes(TeamInvitation invitation) {
        return new ListRes(
                invitation.getId(),
                invitation.getTeamName(),
                invitation.getInviterUsername(),
                invitation.getMessage(),
                invitation.getStatus().name(),
                invitation.getSentAt() != null ? invitation.getSentAt().toString() : null
        );
    }
}
