package com.metaverse.moem.team.domain;

import com.metaverse.moem.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(name = "teams")
public class Team extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 60, unique = true)
    private String name;

    @Column(length = 255)
    private String description;

}
