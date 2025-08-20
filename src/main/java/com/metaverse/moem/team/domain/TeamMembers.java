package com.metaverse.moem.team.domain;

import com.metaverse.moem.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "team_member")
public class TeamMembers extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(length = 20, nullable = false)
    private String name;

    @Column(length = 20, nullable = false)
    private String role;

    @Column(length = 20, nullable = false)
    private String status;

    @Column(name = "join_at", nullable = false)
    private LocalDateTime joinAt;

    public void updateRole(String role) {
        this.role = role;
    }

    public void updateName(String name) {
        this.name = name;
    }

}
