package com.metaverse.moem.matching.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.metaverse.moem.auth.domain.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "user_posts")
public class UserPost {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String aiSummary;

    // 🔥 Auth와 1:1 연관관계 설정
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auth_id", nullable = false)
    @JsonIgnore
    private User auth;

    @Column(length = 500)
    private String intro;

    @ElementCollection
    @CollectionTable(name = "user_skills", joinColumns = @JoinColumn(name = "user_post_id"))
    @Column(name = "skill")
    @Builder.Default
    private List<String> skills = new ArrayList<>();

    @Column(length = 100)
    private String contactType;

    @Column(length = 255)
    private String contactValue;

    @Column(length = 100)
    private String workStyle;

    @Column(length = 100)
    private String collaborationPeriod;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // 🔥 편의 메서드 추가
    public String getEmail() {
        return auth != null ? auth.getEmail() : null;
    }

    public String getUsername() {
        return auth != null ? auth.getUsername() : null;
    }
}
