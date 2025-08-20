package com.metaverse.moem.project.domain;

import com.metaverse.moem.team.domain.Team;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name; // project name

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProjectType type; // Team or Personal

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;


    @Column(nullable = false)
    private Long ownerId; // team or user ID

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private boolean isDeleted;

}
