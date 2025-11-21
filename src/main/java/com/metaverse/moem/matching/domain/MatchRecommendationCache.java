package com.metaverse.moem.matching.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "match_recommendation_cache",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_post_id", "project_post_id"})
        })
public class MatchRecommendationCache {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_post_id", nullable = false)
    private UserPost userPost;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_post_id", nullable = false)
    private ProjectPost projectPost;

    @Column(columnDefinition = "TEXT")
    private String reasonForProjectOwner;

    @Column(columnDefinition = "TEXT")
    private String reasonForProjectSeeker;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}