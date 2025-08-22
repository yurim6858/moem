package com.metaverse.moem.team.domain;

import com.metaverse.moem.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "meeting")
public class Meeting extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(columnDefinition = "text")
    private String notes;

    @Column(nullable = false)
    private LocalDateTime startAt;

    private LocalDateTime endAt;

    @Builder
    private Meeting(Team team, String title, String notes,
                    LocalDateTime startAt, LocalDateTime endAt) {
        this.team = team;
        this.title = title;
        this.notes = notes;
        this.startAt = startAt;
        this.endAt = endAt;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public void update(String title, String notes,
                       LocalDateTime startAt, LocalDateTime endAt) {
        if (title != null && !title.isBlank()) this.title = title;
        if (notes != null) this.notes = notes;
        if (startAt != null) this.startAt = startAt;
        if (endAt != null) this.endAt = endAt;
    }
}
