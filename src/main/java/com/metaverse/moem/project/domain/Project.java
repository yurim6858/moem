package com.metaverse.moem.project.domain;

import com.metaverse.moem.common.BaseTimeEntity;
import com.metaverse.moem.team.domain.Team;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Setter
@Getter
@Table(name = "project")
public class Project extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false,length = 100)
    private String name;

    @Column(length = 1000)
    private String description;
    private Integer recruitTotal;
    private Integer recruitCurrent;
    private LocalDate recruitStartDate;
    private LocalDate recruitEndDate;
    private LocalDate projectStartDate;
    private LocalDate projectEndDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProjectType type;

    @OneToOne (fetch = FetchType.LAZY, mappedBy = "project")
    private Team team;

    @Column(nullable = false)
    private Long ownerId;

    @Column(nullable = false)
    private boolean isDeleted;

}
