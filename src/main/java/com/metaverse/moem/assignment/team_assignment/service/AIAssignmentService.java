package com.metaverse.moem.assignment.team_assignment.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.metaverse.moem.assignment.team_assignment.domain.TeamAssignment;
import com.metaverse.moem.assignment.team_assignment.dto.TeamAssignmentDto;
import com.metaverse.moem.assignment.team_assignment.repository.TeamAssignmentRepository;
import com.metaverse.moem.matching.service.GeminiService;
import com.metaverse.moem.project.domain.Project;
import com.metaverse.moem.project.repository.ProjectRepository;
import com.metaverse.moem.team.domain.TeamMembers;
import com.metaverse.moem.team.repository.TeamMembersRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
@ConditionalOnBean(GeminiService.class)
public class AIAssignmentService {

    private final GeminiService geminiService;
    private final TeamAssignmentService teamAssignmentService;
    private final ProjectRepository projectRepository;
    private final TeamMembersRepository teamMembersRepository;
    private final TeamAssignmentRepository teamAssignmentRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * AI를 사용하여 프로젝트 정보를 기반으로 과제를 자동 생성합니다.
     */
    @Transactional
    public List<TeamAssignmentDto.Res> generateAssignments(Long projectId) throws IOException {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("프로젝트가 존재하지 않습니다."));

        // 프로젝트 정보 수집
        String projectInfo = buildProjectInfo(project);
        
        // 팀 멤버 정보 수집
        List<TeamMembers> members = teamMembersRepository.findByTeamId(project.getTeam().getId());
        String membersInfo = buildMembersInfo(members);

        // AI에게 과제 생성 요청
        String prompt = buildAssignmentPrompt(projectInfo, membersInfo, project);
        String aiResponse = geminiService.getCompletion(prompt);

        // AI 응답 파싱 및 과제 생성
        List<AssignmentPlan> assignmentPlans = parseAIResponse(aiResponse);
        
        List<TeamAssignmentDto.Res> createdAssignments = new ArrayList<>();
        for (AssignmentPlan plan : assignmentPlans) {
            try {
                // 각 멤버에게 과제 할당
                Long assignedUserId = findUserIdByUsername(members, plan.assignedTo);
                if (assignedUserId == null) {
                    log.warn("사용자를 찾을 수 없습니다: {}", plan.assignedTo);
                    continue;
                }

                TeamAssignmentDto.CreateReq createReq = new TeamAssignmentDto.CreateReq(
                        projectId,
                        assignedUserId,
                        plan.title,
                        plan.description,
                        plan.dueDate
                );

                TeamAssignmentDto.Res created = teamAssignmentService.create(createReq);
                createdAssignments.add(created);
            } catch (Exception e) {
                log.error("과제 생성 실패: {}", e.getMessage(), e);
            }
        }

        return createdAssignments;
    }

    /**
     * AI를 사용하여 프로젝트 일정을 자동 생성합니다.
     */
    @Transactional
    public List<TeamAssignmentDto.Res> generateSchedule(Long projectId) throws IOException {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("프로젝트가 존재하지 않습니다."));

        // 기존 과제 확인
        List<TeamAssignment> existingAssignments = teamAssignmentRepository.findAllByProjectId(projectId);
        
        // 프로젝트 정보 수집
        String projectInfo = buildProjectInfo(project);
        
        // 팀 멤버 정보 수집
        List<TeamMembers> members = teamMembersRepository.findByTeamId(project.getTeam().getId());
        String membersInfo = buildMembersInfo(members);

        // AI에게 일정 생성 요청
        String prompt = buildSchedulePrompt(projectInfo, membersInfo, project, existingAssignments);
        String aiResponse = geminiService.getCompletion(prompt);

        // AI 응답 파싱 및 일정 생성
        List<AssignmentPlan> schedulePlans = parseAIResponse(aiResponse);
        
        List<TeamAssignmentDto.Res> createdSchedules = new ArrayList<>();
        for (AssignmentPlan plan : schedulePlans) {
            try {
                Long assignedUserId = findUserIdByUsername(members, plan.assignedTo);
                if (assignedUserId == null) {
                    log.warn("사용자를 찾을 수 없습니다: {}", plan.assignedTo);
                    continue;
                }

                TeamAssignmentDto.CreateReq createReq = new TeamAssignmentDto.CreateReq(
                        projectId,
                        assignedUserId,
                        plan.title,
                        plan.description,
                        plan.dueDate
                );

                TeamAssignmentDto.Res created = teamAssignmentService.create(createReq);
                createdSchedules.add(created);
            } catch (Exception e) {
                log.error("일정 생성 실패: {}", e.getMessage(), e);
            }
        }

        return createdSchedules;
    }

    private String buildProjectInfo(Project project) {
        StringBuilder sb = new StringBuilder();
        sb.append("프로젝트 이름: ").append(project.getName()).append("\n");
        if (project.getDescription() != null) {
            sb.append("프로젝트 설명: ").append(project.getDescription()).append("\n");
        }
        if (project.getProjectStartDate() != null) {
            sb.append("프로젝트 시작일: ").append(project.getProjectStartDate()).append("\n");
        }
        if (project.getProjectEndDate() != null) {
            sb.append("프로젝트 종료일: ").append(project.getProjectEndDate()).append("\n");
        } else {
            // 종료일이 없으면 시작일로부터 3개월 후를 예상 종료일로 설정
            if (project.getProjectStartDate() != null) {
                LocalDate estimatedEndDate = project.getProjectStartDate().plusMonths(3);
                sb.append("예상 종료일: ").append(estimatedEndDate).append("\n");
            }
        }
        sb.append("프로젝트 타입: ").append(project.getType()).append("\n");
        return sb.toString();
    }

    private String buildMembersInfo(List<TeamMembers> members) {
        if (members.isEmpty()) {
            return "팀 멤버 정보 없음";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("팀 멤버 (").append(members.size()).append("명):\n");
        for (TeamMembers member : members) {
            sb.append("- 사용자 ID: ").append(member.getUserId())
              .append(", 역할: ").append(member.getRole()).append("\n");
        }
        return sb.toString();
    }

    private String buildAssignmentPrompt(String projectInfo, String membersInfo, Project project) {
        LocalDate startDate = project.getProjectStartDate() != null 
                ? project.getProjectStartDate() 
                : LocalDate.now();
        LocalDate endDate = project.getProjectEndDate() != null 
                ? project.getProjectEndDate() 
                : startDate.plusMonths(3);

        return String.format("""
            다음 프로젝트 정보를 바탕으로 적절한 과제들을 생성해주세요.
            
            %s
            
            %s
            
            프로젝트 기간: %s ~ %s
            
            다음 JSON 형식으로 응답해주세요:
            [
              {
                "title": "과제 제목",
                "description": "과제 상세 설명",
                "assignedTo": "사용자 ID (숫자)",
                "dueDate": "YYYY-MM-DDTHH:mm:ss 형식의 날짜"
              }
            ]
            
            요구사항:
            1. 프로젝트 설명과 타입을 고려하여 실용적인 과제들을 생성하세요.
            2. 각 과제는 명확한 제목과 설명을 가져야 합니다.
            3. 과제는 프로젝트 기간 내에 분배되어야 합니다.
            4. 최소 3개 이상, 최대 10개 이하의 과제를 생성하세요.
            5. dueDate는 프로젝트 시작일과 종료일 사이여야 합니다.
            6. assignedTo는 제공된 사용자 ID 중 하나여야 합니다.
            
            JSON 형식만 응답하고, 다른 설명은 포함하지 마세요.
            """, projectInfo, membersInfo, startDate, endDate);
    }

    private String buildSchedulePrompt(String projectInfo, String membersInfo, Project project, 
                                      List<TeamAssignment> existingAssignments) {
        LocalDate startDate = project.getProjectStartDate() != null 
                ? project.getProjectStartDate() 
                : LocalDate.now();
        LocalDate endDate = project.getProjectEndDate() != null 
                ? project.getProjectEndDate() 
                : startDate.plusMonths(3);

        StringBuilder existingInfo = new StringBuilder();
        if (!existingAssignments.isEmpty()) {
            existingInfo.append("기존 과제 목록:\n");
            for (TeamAssignment assignment : existingAssignments) {
                existingInfo.append("- ").append(assignment.getTitle())
                           .append(" (마감일: ").append(assignment.getDueAt()).append(")\n");
            }
        }

        return String.format("""
            다음 프로젝트 정보를 바탕으로 프로젝트 일정을 생성해주세요.
            
            %s
            
            %s
            
            %s
            
            프로젝트 기간: %s ~ %s
            
            다음 JSON 형식으로 응답해주세요:
            [
              {
                "title": "일정/마일스톤 제목",
                "description": "일정 상세 설명",
                "assignedTo": "사용자 ID (숫자)",
                "dueDate": "YYYY-MM-DDTHH:mm:ss 형식의 날짜"
              }
            ]
            
            요구사항:
            1. 프로젝트의 주요 마일스톤과 일정을 생성하세요.
            2. 기존 과제와 중복되지 않도록 주의하세요.
            3. 일정은 프로젝트 기간 내에 분배되어야 합니다.
            4. 최소 3개 이상, 최대 8개 이하의 일정을 생성하세요.
            5. dueDate는 프로젝트 시작일과 종료일 사이여야 합니다.
            
            JSON 형식만 응답하고, 다른 설명은 포함하지 마세요.
            """, projectInfo, membersInfo, existingInfo.toString(), startDate, endDate);
    }

    private List<AssignmentPlan> parseAIResponse(String aiResponse) throws IOException {
        try {
            // JSON 배열 파싱 시도
            String cleanedResponse = aiResponse.trim();
            
            // 마크다운 코드 블록 제거
            if (cleanedResponse.startsWith("```json")) {
                cleanedResponse = cleanedResponse.substring(7);
            }
            if (cleanedResponse.startsWith("```")) {
                cleanedResponse = cleanedResponse.substring(3);
            }
            if (cleanedResponse.endsWith("```")) {
                cleanedResponse = cleanedResponse.substring(0, cleanedResponse.length() - 3);
            }
            cleanedResponse = cleanedResponse.trim();

            List<Map<String, Object>> rawList = objectMapper.readValue(
                    cleanedResponse, 
                    new TypeReference<List<Map<String, Object>>>() {}
            );

            List<AssignmentPlan> plans = new ArrayList<>();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
            
            for (Map<String, Object> item : rawList) {
                AssignmentPlan plan = new AssignmentPlan();
                plan.title = (String) item.get("title");
                plan.description = (String) item.get("description");
                
                // assignedTo 처리 (문자열 또는 숫자)
                Object assignedToObj = item.get("assignedTo");
                if (assignedToObj instanceof Number) {
                    plan.assignedTo = assignedToObj.toString();
                } else {
                    plan.assignedTo = (String) assignedToObj;
                }
                
                // dueDate 처리
                String dueDateStr = (String) item.get("dueDate");
                try {
                    plan.dueDate = LocalDateTime.parse(dueDateStr, formatter);
                } catch (Exception e) {
                    // 다른 형식 시도
                    try {
                        plan.dueDate = LocalDateTime.parse(dueDateStr);
                    } catch (Exception e2) {
                        log.warn("날짜 파싱 실패: {}", dueDateStr);
                        plan.dueDate = LocalDateTime.now().plusDays(7); // 기본값
                    }
                }
                
                plans.add(plan);
            }
            
            return plans;
        } catch (Exception e) {
            log.error("AI 응답 파싱 실패: {}", aiResponse, e);
            throw new IOException("AI 응답을 파싱할 수 없습니다: " + e.getMessage(), e);
        }
    }

    private Long findUserIdByUsername(List<TeamMembers> members, String userIdStr) {
        try {
            Long userId = Long.parseLong(userIdStr);
            return members.stream()
                    .filter(m -> m.getUserId().equals(userId))
                    .map(TeamMembers::getUserId)
                    .findFirst()
                    .orElse(null);
        } catch (NumberFormatException e) {
            log.warn("사용자 ID 파싱 실패: {}", userIdStr);
            return null;
        }
    }

    private static class AssignmentPlan {
        String title;
        String description;
        String assignedTo;
        LocalDateTime dueDate;
    }
}

