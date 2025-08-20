package com.metaverse.moem.project.domain;

import com.metaverse.moem.common.BaseTimeEntity;
import com.metaverse.moem.team.domain.Team;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Project extends BaseTimeEntity {

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

    private boolean isDeleted;

}
