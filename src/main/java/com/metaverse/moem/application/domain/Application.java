package com.metaverse.moem.application.domain;

import com.metaverse.moem.auth.domain.User;
import com.metaverse.moem.matching.domain.ProjectPost;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "applications")
public class Application {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🔥 프로젝트와 연관관계 설정
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private ProjectPost project;

    // 🔥 지원자와 연관관계 설정 (Auth와 직접 연결)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "applicant_id", nullable = false)
    private User applicant;

    @Column(length = 500)
    private String message;

    @Column(length = 100)
    private String appliedPosition; // 지원한 포지션

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationStatus status;

    private LocalDateTime appliedAt;
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        appliedAt = LocalDateTime.now();
        updatedAt = appliedAt;
        if (status == null) {
            status = ApplicationStatus.PENDING;
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // 🔥 편의 메서드 추가
    public Long getProjectId() {
        return project != null ? project.getId() : null;
    }

    public String getProjectTitle() {
        return project != null ? project.getTitle() : null;
    }

    public String getApplicantUsername() {
        return applicant != null ? applicant.getUsername() : null;
    }

    public String getApplicantEmail() {
        return applicant != null ? applicant.getEmail() : null;
    }

    public Long getApplicantId() {
        return applicant != null ? applicant.getId() : null;
    }

    // 🔥 비즈니스 로직 메서드
    public boolean isFromSameUser(User user) {
        return applicant != null && applicant.getId().equals(user.getId());
    }

    public boolean canBeApprovedBy(User user) {
        return project != null && project.getCreator() != null && project.getCreator().getId().equals(user.getId());
    }

    public enum ApplicationStatus {
        PENDING,    // 대기중
        APPROVED,   // 승인됨
        REJECTED,   // 거절됨
        WITHDRAWN   // 지원 취소
    }
}

