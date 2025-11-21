package com.metaverse.moem.assignment.team_assignment.domain;

import com.metaverse.moem.common.BaseTimeEntity;
import com.metaverse.moem.project.domain.Project;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "weekly_report")
public class WeeklyReport extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(nullable = false)
    private LocalDate weekStartDate; // 주 시작일 (월요일)

    @Column(nullable = false)
    private LocalDate weekEndDate; // 주 종료일 (일요일)

    @Column(nullable = false)
    private Integer weekNumber; // 주차 번호

    // 성과 지표 (JSON 형식)
    @Column(columnDefinition = "text")
    private String performanceData; // 완료율, 지연율 등

    // 협업 지표 (JSON 형식)
    @Column(columnDefinition = "text")
    private String collaborationData; // 회의 참여율, 커뮤니케이션 등

    // 태도 지표 (JSON 형식)
    @Column(columnDefinition = "text")
    private String attitudeData; // 응답 시간, 적극성 등

    // AI 분석 결과
    @Column(columnDefinition = "text")
    private String aiAnalysis; // AI가 생성한 분석 리포트

    public void setProject(Project project) {
        this.project = project;
    }
}

