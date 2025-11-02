package com.metaverse.moem.team.service;

import com.metaverse.moem.auth.domain.User;
import com.metaverse.moem.auth.repository.UserRepository;
import com.metaverse.moem.team.domain.Team;
import com.metaverse.moem.team.domain.TeamInvitation;
import com.metaverse.moem.team.dto.TeamInvitationDto;
import com.metaverse.moem.team.repository.TeamInvitationRepository;
import com.metaverse.moem.team.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamInvitationService {

    private final TeamInvitationRepository teamInvitationRepository;
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    private final TeamMembersService teamMembersService;

    // 팀 초대 발송
    @Transactional
    public TeamInvitationDto.Res sendInvitation(Long teamId, TeamInvitationDto.CreateReq request, String inviterUsername) {
        // 팀 조회
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("팀을 찾을 수 없습니다."));

        // 초대받을 사용자 조회
        User invitedUser = userRepository.findById(request.invitedUserId())
                .orElseThrow(() -> new IllegalArgumentException("초대받을 사용자를 찾을 수 없습니다."));

        // 초대하는 사용자 조회
        User inviter = userRepository.findByUsername(inviterUsername)
                .orElseThrow(() -> new IllegalArgumentException("초대하는 사용자를 찾을 수 없습니다."));

        // 중복 초대 체크
        if (teamInvitationRepository.hasActiveInvitation(teamId, invitedUser)) {
            throw new IllegalArgumentException("이미 해당 사용자에게 활성화된 초대가 있습니다.");
        }

        // 팀 멤버 중복 체크
        if (isAlreadyTeamMember(teamId, invitedUser.getId())) {
            throw new IllegalArgumentException("이미 팀 멤버입니다.");
        }

        // 초대 생성
        TeamInvitation invitation = TeamInvitation.builder()
                .team(team)
                .invitedUser(invitedUser)
                .inviter(inviter)
                .message(request.message())
                .status(TeamInvitation.InvitationStatus.PENDING)
                .build();

        TeamInvitation savedInvitation = teamInvitationRepository.save(invitation);
        return TeamInvitationDto.toRes(savedInvitation);
    }

    // 내가 받은 초대 목록 조회
    public List<TeamInvitationDto.ListRes> getMyInvitations(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        List<TeamInvitation> invitations = teamInvitationRepository.findByInvitedUserOrderBySentAtDesc(user);
        return invitations.stream()
                .map(TeamInvitationDto::toListRes)
                .toList();
    }

    // 내가 받은 대기중인 초대 목록 조회
    public List<TeamInvitationDto.ListRes> getMyPendingInvitations(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        List<TeamInvitation> invitations = teamInvitationRepository.findByInvitedUserAndStatusOrderBySentAtDesc(
                user, TeamInvitation.InvitationStatus.PENDING);
        return invitations.stream()
                .map(TeamInvitationDto::toListRes)
                .toList();
    }

    // 초대 상세 조회
    public TeamInvitationDto.Res getInvitation(Long invitationId, String username) {
        TeamInvitation invitation = teamInvitationRepository.findById(invitationId)
                .orElseThrow(() -> new IllegalArgumentException("초대를 찾을 수 없습니다."));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 본인의 초대만 조회 가능
        if (!invitation.canBeRespondedBy(user)) {
            throw new IllegalArgumentException("본인의 초대만 조회할 수 있습니다.");
        }

        return TeamInvitationDto.toRes(invitation);
    }

    // 초대 응답 (수락/거절)
    @Transactional
    public TeamInvitationDto.Res respondToInvitation(Long invitationId, TeamInvitationDto.RespondReq request, String username) {
        TeamInvitation invitation = teamInvitationRepository.findById(invitationId)
                .orElseThrow(() -> new IllegalArgumentException("초대를 찾을 수 없습니다."));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 본인의 초대만 응답 가능
        if (!invitation.canBeRespondedBy(user)) {
            throw new IllegalArgumentException("본인의 초대만 응답할 수 있습니다.");
        }

        // 이미 응답한 초대는 재응답 불가
        if (!invitation.isPending()) {
            throw new IllegalArgumentException("이미 응답한 초대입니다.");
        }

        // 응답 처리
        if ("ACCEPTED".equals(request.status())) {
            invitation.accept();
            
            // 팀 멤버로 추가
            addToTeam(invitation);
            
        } else if ("DECLINED".equals(request.status())) {
            invitation.decline();
        } else {
            throw new IllegalArgumentException("유효하지 않은 응답 상태입니다.");
        }

        TeamInvitation updatedInvitation = teamInvitationRepository.save(invitation);
        
        // 응답 후 초대 삭제 (수락/거절 모두)
        teamInvitationRepository.delete(invitation);
        
        return TeamInvitationDto.toRes(updatedInvitation);
    }

    // 팀의 초대 목록 조회 (팀 관리자용)
    public List<TeamInvitationDto.Res> getTeamInvitations(Long teamId) {
        List<TeamInvitation> invitations = teamInvitationRepository.findByTeam_IdOrderBySentAtDesc(teamId);
        return invitations.stream()
                .map(TeamInvitationDto::toRes)
                .toList();
    }

    // 초대 취소 (팀 관리자용)
    @Transactional
    public void cancelInvitation(Long invitationId, String username) {
        TeamInvitation invitation = teamInvitationRepository.findById(invitationId)
                .orElseThrow(() -> new IllegalArgumentException("초대를 찾을 수 없습니다."));

        // TODO: 팀 관리자 권한 체크 로직 추가 필요

        // 대기중인 초대만 취소 가능
        if (!invitation.isPending()) {
            throw new IllegalArgumentException("대기중인 초대만 취소할 수 있습니다.");
        }

        invitation.setStatus(TeamInvitation.InvitationStatus.DECLINED);
        invitation.setRespondedAt(java.time.LocalDateTime.now());
        teamInvitationRepository.save(invitation);
    }

    // 팀 멤버 추가 (초대 수락 시)
    private void addToTeam(TeamInvitation invitation) {
        try {
            // 초대 메시지에서 포지션 정보 추출
            String positionStr = extractPositionFromMessage(invitation.getMessage());
            
            // Role enum으로 변환 (기본값은 MEMBER)
            com.metaverse.moem.team.domain.Role role = com.metaverse.moem.team.domain.Role.MEMBER;
            if (positionStr != null) {
                try {
                    role = com.metaverse.moem.team.domain.Role.valueOf(positionStr.toUpperCase());
                } catch (IllegalArgumentException e) {
                    // 유효하지 않은 Role이면 기본값 사용
                    role = com.metaverse.moem.team.domain.Role.MEMBER;
                }
            }
            
            // TeamMembersService를 통해 팀 멤버 추가
            com.metaverse.moem.team.dto.TeamMembersDto.CreateReq createReq = 
                new com.metaverse.moem.team.dto.TeamMembersDto.CreateReq(
                    invitation.getInvitedUser().getId()
                );
            
            teamMembersService.create(invitation.getTeam().getId(), createReq, role);
        } catch (Exception e) {
            // 팀 멤버 추가 실패 시 초대 상태 롤백
            invitation.setStatus(TeamInvitation.InvitationStatus.PENDING);
            invitation.setRespondedAt(null);
            throw new RuntimeException("팀 멤버 추가에 실패했습니다: " + e.getMessage());
        }
    }

    // 초대 메시지에서 포지션 정보 추출
    private String extractPositionFromMessage(String message) {
        if (message == null) return null;
        
        // "포지션: 프론트엔드 개발자" 형태에서 포지션 추출
        String[] parts = message.split("포지션:");
        if (parts.length > 1) {
            String positionPart = parts[1].split("\\.")[0].trim();
            return positionPart.isEmpty() ? null : positionPart;
        }
        
        return null;
    }

    // 이미 팀 멤버인지 확인
    private boolean isAlreadyTeamMember(Long teamId, Long userId) {
        try {
            List<com.metaverse.moem.team.dto.TeamMembersDto.Res> members = 
                teamMembersService.list(teamId);
            return members.stream()
                    .anyMatch(member -> member.userId().equals(userId));
        } catch (Exception e) {
            return false;
        }
    }
}
