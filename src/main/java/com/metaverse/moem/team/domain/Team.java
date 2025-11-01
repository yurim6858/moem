package com.metaverse.moem.team.domain;

import com.metaverse.moem.common.BaseTimeEntity;
import com.metaverse.moem.project.domain.Project;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "team")
public class Team extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false, unique = true)
    private Project project;

    @Column(nullable = false, length = 50)
    private String  name;

    @Column(nullable = false)
    private Integer maxMembers;

    @OneToMany(mappedBy = "team", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TeamMembers> members = new ArrayList<>();

    @OneToMany (mappedBy = "team",cascade = CascadeType.ALL, orphanRemoval = true)
    private  List<Meeting> meetings = new ArrayList<>();

    @Builder
    private Team(Project project, String name, Integer maxMembers) {
        this.project = project;
        this.name = name;
        this.maxMembers = maxMembers == null ? 0 : maxMembers;
    }

    public static Team create(Project project, String name, Integer maxMembers) {
        Team team = new Team();
        team.project = project;
        team.maxMembers = maxMembers;
        team.name = (name == null || name.isBlank())
                ? project.getName()
                : name;
        return team;
    }

    public void addMember(TeamMembers member) {
        if (member == null) return;
        members.add(member);
        member.setTeam(this);
    }

    public void removeMember(TeamMembers member) {
        if (member == null) return;
        members.remove(member);
        member.setTeam(null);
    }

    public void addMeeting(Meeting meeting) {
        if (meeting == null) return;
        meetings.add(meeting);
        meeting.setTeam(this);
    }

    public void removeMeeting(Meeting meeting) {
        if (meeting == null) return;
        meetings.remove(meeting);
        meeting.setTeam(null);
    }

    public void changeProject(Project project) {
        this.project = project;
    }

    public void updateInfo(String name, Integer maxMembers) {
        if (name != null && !name.isBlank()) this.name = name;
        if (maxMembers != null) this.maxMembers = maxMembers;
    }
}
