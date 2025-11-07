package com.metaverse.moem.team.service;

import com.metaverse.moem.auth.domain.User;
import com.metaverse.moem.auth.repository.UserRepository;
import com.metaverse.moem.team.domain.Team;
import com.metaverse.moem.team.domain.TeamMemberLeaveRequest;
import com.metaverse.moem.team.domain.TeamMembers;
import com.metaverse.moem.team.dto.TeamMemberLeaveRequestDto;
import com.metaverse.moem.team.repository.TeamMemberLeaveRequestRepository;
import com.metaverse.moem.team.repository.TeamMembersRepository;
import com.metaverse.moem.team.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TeamMemberLeaveRequestService {

    private final TeamMemberLeaveRequestRepository leaveRequestRepository;
    private final TeamMembersRepository teamMembersRepository;
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    private final TeamMembersService teamMembersService;

    // 팀원 나가기 요청 생성
    @Transactional
    public TeamMemberLeaveRequestDto.Res createLeaveRequest(Long teamId, Long teamMemberId, 
                                                             TeamMemberLeaveRequestDto.CreateReq req, 
                                                             String username) {
        // 팀 조회
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("팀을 찾을 수 없습니다."));

        // 팀원 조회
        TeamMembers teamMember = teamMembersRepository.findById(teamMemberId)
                .orElseThrow(() -> new IllegalArgumentException("팀원을 찾을 수 없습니다."));

        // 요청자가 본인인지 확인
        User requester = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (!teamMember.getUserId().equals(requester.getId())) {
            throw new IllegalArgumentException("본인의 나가기 요청만 생성할 수 있습니다.");
        }

        // 팀원이 해당 팀에 속해있는지 확인
        if (!teamMember.getTeam().getId().equals(teamId)) {
            throw new IllegalArgumentException("해당 팀의 팀원이 아닙니다.");
        }

        // 리더는 나가기 요청 불가
        if (teamMember.getRole() == com.metaverse.moem.team.domain.Role.MANAGER) {
            throw new IllegalArgumentException("팀 리더는 나가기 요청을 할 수 없습니다.");
        }

        // 이미 대기중인 요청이 있는지 확인
        leaveRequestRepository.findByTeamMember_IdAndStatus(
                teamMemberId, TeamMemberLeaveRequest.LeaveRequestStatus.PENDING)
                .ifPresent(request -> {
                    throw new IllegalArgumentException("이미 대기중인 나가기 요청이 있습니다.");
                });

        // 나가기 요청 생성
        TeamMemberLeaveRequest leaveRequest = TeamMemberLeaveRequest.builder()
                .team(team)
                .teamMember(teamMember)
                .reason(req.reason())
                .status(TeamMemberLeaveRequest.LeaveRequestStatus.PENDING)
                .build();

        TeamMemberLeaveRequest saved = leaveRequestRepository.save(leaveRequest);
        return TeamMemberLeaveRequestDto.Res.from(saved, userRepository);
    }

    // 팀의 나가기 요청 목록 조회 (리더용)
    public List<TeamMemberLeaveRequestDto.Res> getLeaveRequestsByTeam(Long teamId, String username) {
        // 팀 존재 확인
        if (!teamRepository.existsById(teamId)) {
            throw new IllegalArgumentException("팀을 찾을 수 없습니다.");
        }

        // 요청자가 리더인지 확인
        User requester = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (!isTeamLeader(teamId, requester.getId())) {
            throw new IllegalArgumentException("팀 리더만 나가기 요청 목록을 조회할 수 있습니다.");
        }

        List<TeamMemberLeaveRequest> requests = leaveRequestRepository.findByTeam_IdOrderByCreatedAtDesc(teamId);
        return requests.stream()
                .map(request -> TeamMemberLeaveRequestDto.Res.from(request, userRepository))
                .toList();
    }

    // 팀의 대기중인 나가기 요청 목록 조회 (리더용)
    public List<TeamMemberLeaveRequestDto.Res> getPendingLeaveRequestsByTeam(Long teamId, String username) {
        // 팀 존재 확인
        if (!teamRepository.existsById(teamId)) {
            throw new IllegalArgumentException("팀을 찾을 수 없습니다.");
        }

        // 요청자가 리더인지 확인
        User requester = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (!isTeamLeader(teamId, requester.getId())) {
            throw new IllegalArgumentException("팀 리더만 나가기 요청 목록을 조회할 수 있습니다.");
        }

        List<TeamMemberLeaveRequest> requests = leaveRequestRepository.findByTeam_IdAndStatusOrderByCreatedAtDesc(
                teamId, TeamMemberLeaveRequest.LeaveRequestStatus.PENDING);
        return requests.stream()
                .map(request -> TeamMemberLeaveRequestDto.Res.from(request, userRepository))
                .toList();
    }

    // 나가기 요청 승인/거절 (리더용)
    @Transactional
    public TeamMemberLeaveRequestDto.Res respondToLeaveRequest(Long leaveRequestId, 
                                                               TeamMemberLeaveRequestDto.RespondReq req,
                                                               String username) {
        // 나가기 요청 조회
        TeamMemberLeaveRequest leaveRequest = leaveRequestRepository.findById(leaveRequestId)
                .orElseThrow(() -> new IllegalArgumentException("나가기 요청을 찾을 수 없습니다."));

        // 요청자가 리더인지 확인
        User responder = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Long teamId = leaveRequest.getTeam().getId();
        if (!isTeamLeader(teamId, responder.getId())) {
            throw new IllegalArgumentException("팀 리더만 나가기 요청에 응답할 수 있습니다.");
        }

        // 이미 처리된 요청인지 확인
        if (!leaveRequest.isPending()) {
            throw new IllegalArgumentException("이미 처리된 나가기 요청입니다.");
        }

        // 승인 또는 거절 처리
        if ("APPROVED".equalsIgnoreCase(req.status())) {
            leaveRequest.approve();
            leaveRequestRepository.save(leaveRequest);
            
            // 승인 시 팀원 제거
            teamMembersService.delete(leaveRequest.getTeamMember().getId());
        } else if ("REJECTED".equalsIgnoreCase(req.status())) {
            leaveRequest.reject();
            leaveRequestRepository.save(leaveRequest);
        } else {
            throw new IllegalArgumentException("유효하지 않은 상태입니다. APPROVED 또는 REJECTED를 사용하세요.");
        }

        return TeamMemberLeaveRequestDto.Res.from(leaveRequest, userRepository);
    }

    // 팀 리더인지 확인
    private boolean isTeamLeader(Long teamId, Long userId) {
        List<com.metaverse.moem.team.dto.TeamMembersDto.Res> members = teamMembersService.list(teamId);
        return members.stream()
                .anyMatch(member -> member.userId().equals(userId) 
                        && "MANAGER".equals(member.role()));
    }
}

