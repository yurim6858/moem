package com.metaverse.moem.team.domain;

import com.metaverse.moem.auth.domain.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "team_member")
public class TeamMembers {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // team FK 연관관계
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    // user FK 연관관계 - User 엔티티와 직접 연관관계 설정
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

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
    
    // 편의 메서드: userId 반환
    public Long getUserId() {
        return user != null ? user.getId() : null;
    }

}
