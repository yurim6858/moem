package com.metaverse.moem.team.domain;

import com.metaverse.moem.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "team_member",
    uniqueConstraints = @UniqueConstraint(name = "uk_team_member_team_user", columnNames = {"team_id", "user_id"})
)
public class TeamMembers extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_id", nullable = false)
    private Team team;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false,length = 20)
    private Role role;

    public static TeamMembers create(Team team, Long userId, Role role) {
        TeamMembers members = new TeamMembers();
        members.team = team;
        members.userId = userId;
        members.role = (role == null ? Role.MEMBER : role);
        return members;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public void changeRole(Role role) {
        if (role != null) this.role = role;
    }
}
