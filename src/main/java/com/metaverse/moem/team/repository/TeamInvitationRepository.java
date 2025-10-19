package com.metaverse.moem.team.repository;

import com.metaverse.moem.auth.domain.User;
import com.metaverse.moem.team.domain.TeamInvitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamInvitationRepository extends JpaRepository<TeamInvitation, Long> {
    
    // 특정 사용자가 받은 초대 목록 조회
    List<TeamInvitation> findByInvitedUserOrderBySentAtDesc(User invitedUser);
    
    // 특정 사용자가 받은 대기중인 초대 목록 조회
    List<TeamInvitation> findByInvitedUserAndStatusOrderBySentAtDesc(User invitedUser, TeamInvitation.InvitationStatus status);
    
    // 특정 팀에서 특정 사용자에게 보낸 초대 조회
    Optional<TeamInvitation> findByTeam_IdAndInvitedUser(Long teamId, User invitedUser);
    
    // 특정 팀의 모든 초대 조회
    List<TeamInvitation> findByTeam_IdOrderBySentAtDesc(Long teamId);
    
    // 특정 팀의 특정 상태 초대 조회
    List<TeamInvitation> findByTeam_IdAndStatusOrderBySentAtDesc(Long teamId, TeamInvitation.InvitationStatus status);
    
    // 중복 초대 체크 (같은 팀, 같은 사용자, 대기중인 초대)
    boolean existsByTeam_IdAndInvitedUserAndStatus(Long teamId, User invitedUser, TeamInvitation.InvitationStatus status);
    
    // 특정 사용자가 특정 팀에 이미 초대받았는지 확인
    @Query("SELECT COUNT(ti) > 0 FROM TeamInvitation ti WHERE ti.team.id = :teamId AND ti.invitedUser = :user AND ti.status IN ('PENDING', 'ACCEPTED')")
    boolean hasActiveInvitation(@Param("teamId") Long teamId, @Param("user") User user);
}
