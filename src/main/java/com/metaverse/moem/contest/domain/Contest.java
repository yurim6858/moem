package com.metaverse.moem.contest.domain;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "contest")
public class Contest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 100)
    private String host;

    @Column(nullable = false)
    private LocalDate deadline;

    @Column(nullable = false, length = 500)
    private String sourceUrl;

    @Column(length = 1000)
    private String category;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @Builder
    public Contest(String title, String host, LocalDate deadline, String sourceUrl, String category) {
        this.title = title;
        this.host = host;
        this.deadline = deadline;
        this.sourceUrl = sourceUrl;
        this.category = category;
    }
}