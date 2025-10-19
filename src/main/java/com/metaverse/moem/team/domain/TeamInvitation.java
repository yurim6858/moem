package com.metaverse.moem.team.domain;

import com.metaverse.moem.auth.domain.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "team_invitations")
public class TeamInvitation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 팀과 연관관계 설정
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    // 초대받은 사용자 (Auth와 직접 연결)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invited_user_id", nullable = false)
    private User invitedUser;

    // 초대한 사용자 (Auth와 직접 연결)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inviter_id", nullable = false)
    private User inviter;

    @Column(length = 500)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InvitationStatus status;

    @Column(name = "sent_at", nullable = false)
    private LocalDateTime sentAt;

    @Column(name = "responded_at")
    private LocalDateTime respondedAt;

    @PrePersist
    void onCreate() {
        sentAt = LocalDateTime.now();
        if (status == null) {
            status = InvitationStatus.PENDING;
        }
    }

    @PreUpdate
    void onUpdate() {
        if (status != InvitationStatus.PENDING && respondedAt == null) {
            respondedAt = LocalDateTime.now();
        }
    }

    // 편의 메서드
    public Long getTeamId() {
        return team != null ? team.getId() : null;
    }

    public String getTeamName() {
        return team != null ? team.getName() : null;
    }

    public String getInvitedUserUsername() {
        return invitedUser != null ? invitedUser.getUsername() : null;
    }

    public String getInviterUsername() {
        return inviter != null ? inviter.getUsername() : null;
    }

    // 비즈니스 로직 메서드
    public boolean canBeRespondedBy(User user) {
        return invitedUser != null && invitedUser.getId().equals(user.getId());
    }

    public boolean isPending() {
        return status == InvitationStatus.PENDING;
    }

    public boolean isAccepted() {
        return status == InvitationStatus.ACCEPTED;
    }

    public boolean isDeclined() {
        return status == InvitationStatus.DECLINED;
    }

    public void accept() {
        this.status = InvitationStatus.ACCEPTED;
        this.respondedAt = LocalDateTime.now();
    }

    public void decline() {
        this.status = InvitationStatus.DECLINED;
        this.respondedAt = LocalDateTime.now();
    }

    public enum InvitationStatus {
        PENDING,    // 초대 발송됨 (대기중)
        ACCEPTED,   // 수락됨
        DECLINED,   // 거절됨
        EXPIRED     // 만료됨
    }
}
