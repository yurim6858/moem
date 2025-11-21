package com.metaverse.moem.assignment.team_assignment.domain;

import com.metaverse.moem.common.BaseTimeEntity;
import com.metaverse.moem.project.domain.Project;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class TeamAssignment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String title;

    private String description;

    @Column(nullable = false)
    private LocalDateTime dueAt;

    // 평가 및 상태 추적을 위한 필드
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private AssignmentStatus status = AssignmentStatus.TODO;

    @Column(nullable = true)
    @Builder.Default
    private Integer progress = 0; // 0-100%

    @Column(nullable = true)
    private LocalDateTime completedAt;

    @Column(nullable = true)
    @Builder.Default
    private Integer delayDays = 0; // 지연 일수

    @Column(nullable = true, length = 1000)
    private String completionNotes; // 완료 노트

    public void update(String title, String description, LocalDateTime dueAt) {
        this.title = title;
        this.description = description;
        this.dueAt = dueAt;
    }

    public void updateStatus(AssignmentStatus status, Integer progress) {
        this.status = status;
        this.progress = progress;
        if (status == AssignmentStatus.COMPLETED && this.completedAt == null) {
            this.completedAt = LocalDateTime.now();
        }
        // 지연 일수 계산
        if (status != AssignmentStatus.COMPLETED && dueAt.isBefore(LocalDateTime.now())) {
            this.delayDays = (int) java.time.Duration.between(dueAt, LocalDateTime.now()).toDays();
        }
    }

    public void complete(String notes) {
        this.status = AssignmentStatus.COMPLETED;
        this.progress = 100;
        this.completedAt = LocalDateTime.now();
        this.completionNotes = notes;
        this.delayDays = 0;
    }

    public enum AssignmentStatus {
        TODO,           // 할 일
        IN_PROGRESS,    // 진행 중
        COMPLETED,      // 완료
        DELAYED         // 지연
    }
}
