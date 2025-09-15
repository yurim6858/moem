package com.metaverse.moem.matching.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class Matching {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String title;

    @Column
    private String intro;

    @Column
    private String description;

    @ElementCollection
    @CollectionTable(name = "matching_tags", joinColumns = @JoinColumn(name = "matching_id"))
    @Column(name = "tag")
    private List<String> tags = new ArrayList<>();

    private LocalDateTime deadline;

    @Column
    private String username;

    @Column
    private String workStyle;

    @Column
    private String contactType;

    @Column
    private String contactValue;

    @Column
    private String collaborationPeriod;

    @ElementCollection
    @CollectionTable(name = "matching_positions", joinColumns = @JoinColumn(name = "matching_id"))
    @AttributeOverrides({
        @AttributeOverride(name = "role", column = @Column(name = "role")),
        @AttributeOverride(name = "headcount", column = @Column(name = "headcount"))
    })
    private List<Position> positions = new ArrayList<>();

    @Embeddable
    @Getter
    @Setter
    public static class Position {
        private String role;
        private Integer headcount;
    }
}
