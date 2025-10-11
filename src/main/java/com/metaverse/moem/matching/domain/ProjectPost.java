package com.metaverse.moem.matching.domain;

import com.metaverse.moem.auth.domain.Auth;
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
    private Auth creator;

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

    // 🔥 편의 메서드 추가
    public String getCreatorUsername() {
        return creator != null ? creator.getUsername() : null;
    }

    public String getCreatorEmail() {
        return creator != null ? creator.getEmail() : null;
    }

    @Embeddable
    @Getter
    @Setter
    public static class Position {
        private String role;
        private Integer headcount;
    }
}
