package com.metaverse.moem.assignment.team_assignment.service;

import com.metaverse.moem.assignment.team_assignment.domain.TeamAssignment;
import com.metaverse.moem.assignment.team_assignment.repository.TeamAssignmentRepository;
import com.metaverse.moem.gemini.service.GeminiService;
import com.metaverse.moem.project.domain.Project;
import com.metaverse.moem.project.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
@ConditionalOnBean(GeminiService.class)
public class AssignmentReminderService {

    private final TeamAssignmentRepository teamAssignmentRepository;
    private final ProjectRepository projectRepository;
    private final GeminiService geminiService;

    /**
     * 매일 오전 9시에 마감일 리마인드 체크
     * - 마감 3일 전 과제
     * - 마감 1일 전 과제
     * - 오늘 마감인 과제
     */
    @Scheduled(cron = "0 0 9 * * *") // 매일 오전 9시
    @Transactional(readOnly = true)
    public void checkDeadlineReminders() {
        log.info("마감일 리마인드 체크 시작");

        LocalDate today = LocalDate.now();

        // 진행 중인 프로젝트의 과제들 조회
        List<Project> activeProjects = projectRepository.findActiveProjects();

        for (Project project : activeProjects) {
            try {
                List<TeamAssignment> assignments = teamAssignmentRepository.findAllByProjectId(project.getId());

                for (TeamAssignment assignment : assignments) {
                    // 완료된 과제는 제외
                    if (assignment.getStatus() == TeamAssignment.AssignmentStatus.COMPLETED) {
                        continue;
                    }

                    LocalDate dueDate = assignment.getDueAt().toLocalDate();
                    long daysUntilDue = java.time.temporal.ChronoUnit.DAYS.between(today, dueDate);

                    // 마감 3일 전
                    if (daysUntilDue == 3) {
                        sendReminder(assignment, project, 3);
                    }
                    // 마감 1일 전
                    else if (daysUntilDue == 1) {
                        sendReminder(assignment, project, 1);
                    }
                    // 오늘 마감
                    else if (daysUntilDue == 0) {
                        sendReminder(assignment, project, 0);
                    }
                    // 이미 지연된 과제
                    else if (daysUntilDue < 0) {
                        sendDelayAlert(assignment, project, Math.abs(daysUntilDue));
                    }
                }
            } catch (Exception e) {
                log.error("프로젝트 {}의 리마인드 체크 실패", project.getId(), e);
            }
        }

        log.info("마감일 리마인드 체크 완료");
    }

    private void sendReminder(TeamAssignment assignment, Project project, int daysLeft) {
        try {
            generateReminderMessage(assignment, project, daysLeft);
            log.info("리마인드 메시지 생성: 과제 ID={}, 남은 일수={}", assignment.getId(), daysLeft);
            // TODO: 실제 알림 전송 로직 구현 (이메일, 푸시 알림 등)
            // String message = generateReminderMessage(assignment, project, daysLeft);
            // notificationService.sendNotification(assignment.getUserId(), message);
        } catch (Exception e) {
            log.error("리마인드 메시지 생성 실패: 과제 ID={}", assignment.getId(), e);
        }
    }

    private void sendDelayAlert(TeamAssignment assignment, Project project, long delayDays) {
        try {
            generateDelayAlertMessage(assignment, project, delayDays);
            log.warn("지연 알림: 과제 ID={}, 지연 일수={}", assignment.getId(), delayDays);
            // TODO: 실제 알림 전송 로직 구현
            // String message = generateDelayAlertMessage(assignment, project, delayDays);
            // notificationService.sendNotification(assignment.getUserId(), message);
        } catch (Exception e) {
            log.error("지연 알림 메시지 생성 실패: 과제 ID={}", assignment.getId(), e);
        }
    }

    private String generateReminderMessage(TeamAssignment assignment, Project project, int daysLeft) throws IOException {
        String prompt = String.format("""
            다음 과제의 마감일 알림 메시지를 작성해주세요:
            
            프로젝트: %s
            과제: %s
            담당자 ID: %d
            마감일: %s
            남은 일수: %d일
            
            친근하고 격려하는 톤으로 알림 메시지를 작성해주세요.
            간단하고 명확하게 작성해주세요.
            """, project.getName(), assignment.getTitle(), 
            assignment.getUserId(), assignment.getDueAt(), daysLeft);

        return geminiService.getCompletion(prompt);
    }

    private String generateDelayAlertMessage(TeamAssignment assignment, Project project, long delayDays) throws IOException {
        String prompt = String.format("""
            다음 과제가 지연되었습니다. 경고 메시지를 작성해주세요:
            
            프로젝트: %s
            과제: %s
            담당자 ID: %d
            마감일: %s
            지연 일수: %d일
            
            경고하되 건설적인 톤으로 메시지를 작성해주세요.
            """, project.getName(), assignment.getTitle(), 
            assignment.getUserId(), assignment.getDueAt(), delayDays);

        return geminiService.getCompletion(prompt);
    }
}

