package com.metaverse.moem.team.domain;

import com.metaverse.moem.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "team_member_leave_request")
public class TeamMemberLeaveRequest extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_member_id", nullable = false)
    private TeamMembers teamMember;

    @Column(length = 500)
    private String reason;  // 나가기 사유

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LeaveRequestStatus status;

    private LocalDateTime respondedAt;  // 리더 응답 시간

    @PrePersist
    void onCreate() {
        if (status == null) {
            status = LeaveRequestStatus.PENDING;
        }
    }

    public void approve() {
        this.status = LeaveRequestStatus.APPROVED;
        this.respondedAt = LocalDateTime.now();
    }

    public void reject() {
        this.status = LeaveRequestStatus.REJECTED;
        this.respondedAt = LocalDateTime.now();
    }

    public boolean isPending() {
        return status == LeaveRequestStatus.PENDING;
    }

    public enum LeaveRequestStatus {
        PENDING,    // 대기중
        APPROVED,   // 승인됨
        REJECTED    // 거절됨
    }
}

