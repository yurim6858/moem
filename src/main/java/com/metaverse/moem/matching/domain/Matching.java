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
    private String description;

    @ElementCollection
    @CollectionTable(name = "matching_tags", joinColumns = @JoinColumn(name = "matching_id"))
    @Column(name = "tag")
    private List<String> tags = new ArrayList<>();

    private LocalDateTime deadline;

    @Column
    private String username;
}
