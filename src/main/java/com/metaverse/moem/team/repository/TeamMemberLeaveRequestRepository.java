package com.metaverse.moem.team.repository;

import com.metaverse.moem.team.domain.TeamMemberLeaveRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TeamMemberLeaveRequestRepository extends JpaRepository<TeamMemberLeaveRequest, Long> {
    
    // 팀의 모든 나가기 요청 조회
    List<TeamMemberLeaveRequest> findByTeam_IdOrderByCreatedAtDesc(Long teamId);
    
    // 팀의 대기중인 나가기 요청 조회
    List<TeamMemberLeaveRequest> findByTeam_IdAndStatusOrderByCreatedAtDesc(
            Long teamId, TeamMemberLeaveRequest.LeaveRequestStatus status);
    
    // 특정 팀원의 나가기 요청 조회 (단일)
    Optional<TeamMemberLeaveRequest> findByTeamMember_Id(Long teamMemberId);
    
    // 특정 팀원의 대기중인 나가기 요청 조회
    Optional<TeamMemberLeaveRequest> findByTeamMember_IdAndStatus(
            Long teamMemberId, TeamMemberLeaveRequest.LeaveRequestStatus status);
    
    // 특정 팀원의 모든 나가기 요청 조회 (리스트)
    List<TeamMemberLeaveRequest> findAllByTeamMember_Id(Long teamMemberId);
}

