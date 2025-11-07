package com.metaverse.moem.matching.domain;

import com.metaverse.moem.auth.domain.User;
import com.metaverse.moem.team.domain.Team;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "project_posts")
public class ProjectPost {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String title;

    @Column
    private String intro;

    @Column
    private String description;

    @ElementCollection
    @CollectionTable(name = "project_tags", joinColumns = @JoinColumn(name = "project_id"))
    @Column(name = "tag")
    private List<String> tags = new ArrayList<>();

    private LocalDateTime deadline;

    // 🔥 작성자와 연관관계 설정 (Auth 직접 참조)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    @Column
    private String workStyle;

    @Column
    private String contactType;

    @Column
    private String contactValue;

    @Column
    private String collaborationPeriod;

    @ElementCollection
    @CollectionTable(name = "project_positions", joinColumns = @JoinColumn(name = "project_id"))
    @AttributeOverrides({
        @AttributeOverride(name = "role", column = @Column(name = "role")),
        @AttributeOverride(name = "headcount", column = @Column(name = "headcount"))
    })
    private List<Position> positions = new ArrayList<>();

    // 🔥 팀과 연관관계 설정 (프로젝트당 하나의 팀)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id")
    private Team team;
    
    @Column(nullable = false)
    private boolean isDeleted = false;  // Soft delete를 위한 필드
    
    @Column(nullable = false)
    private boolean isRecruitmentCompleted = false;  // 모집 완료 여부

    // 편의 메서드: 삭제 표시
    public void setDeleted(boolean deleted) {
        this.isDeleted = deleted;
    }

    // 편의 메서드: 삭제 여부 확인
    public boolean isDeleted() {
        return isDeleted;
    }
    
    // 편의 메서드: 모집 완료 표시
    public void setRecruitmentCompleted(boolean completed) {
        this.isRecruitmentCompleted = completed;
    }
    
    // 편의 메서드: 모집 완료 여부 확인
    public boolean isRecruitmentCompleted() {
        return isRecruitmentCompleted;
    }

    // 🔥 편의 메서드 추가
    public String getCreatorUsername() {
        return creator != null ? creator.getUsername() : null;
    }

    public String getCreatorEmail() {
        return creator != null ? creator.getEmail() : null;
    }

    public Long getTeamId() {
        return team != null ? team.getId() : null;
    }

    public String getTeamName() {
        return team != null ? team.getName() : null;
    }

    @Embeddable
    @Getter
    @Setter
    public static class Position {
        private String role;
        private Integer headcount;
    }
}
