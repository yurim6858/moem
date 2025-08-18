package com.metaverse.moem.team.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "user")
public class User {
    @Id
    private long id;

    @Column(length = 120, nullable = false)
    private String email;

    @Column(length = 60, nullable = false)
    private String name;
}
