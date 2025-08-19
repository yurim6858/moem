package com.metaverse.moem.assignment.personal_assignment.domain;

import com.metaverse.moem.assignment.team_assignment.domain.TeamAssignment;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.cglib.core.Local;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PersonalAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // team_assignment 기반으로 생성된 과제
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_assignment_id")
    private TeamAssignment teamAssignment;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String title;

    private String description;

    @Column(nullable = false)
    private LocalDateTime dueAt;

    // 직접 생성한 과제
    @Column(nullable = false)
    private boolean userCreated;

    public void update(String title, String description, LocalDateTime dueAt) {
        this.title = title;
        this.description = description;
        this.dueAt = dueAt;
    }

}
