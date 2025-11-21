package com.metaverse.moem.assignment.team_assignment.controller;

import com.metaverse.moem.assignment.team_assignment.dto.TeamAssignmentDto;
import com.metaverse.moem.assignment.team_assignment.service.AIAssignmentService;
import com.metaverse.moem.assignment.team_assignment.service.TeamAssignmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/team-assignments")
@RequiredArgsConstructor
public class TeamAssignmentController {

    private final TeamAssignmentService teamAssignmentService;
    private final Optional<AIAssignmentService> aiAssignmentService;

    @PostMapping
    public TeamAssignmentDto.Res create(@RequestBody @Valid TeamAssignmentDto.CreateReq req) {
        return teamAssignmentService.create(req);
    }

    @GetMapping("/project/{projectId}")
    public List<TeamAssignmentDto.Res> getByProject(@PathVariable Long projectId) {
        return teamAssignmentService.getByProject(projectId);
    }

    @GetMapping("/project/{projectId}/user/{userId}")
    public List<TeamAssignmentDto.Res> getByProjectAndUser(@PathVariable Long projectId,
                                                           @PathVariable Long userId) {
        return teamAssignmentService.getByProjectAndUser(projectId, userId);
    }

    @PutMapping("/{teamAssignmentId}")
    public TeamAssignmentDto.Res update(@PathVariable Long teamAssignmentId,
                                        @RequestBody @Valid TeamAssignmentDto.UpdateReq req) {
        return teamAssignmentService.update(teamAssignmentId, req);
    }

    @DeleteMapping("/{teamAssignmentId}")
    public void delete(@PathVariable Long teamAssignmentId) {
        teamAssignmentService.delete(teamAssignmentId);
    }

    /**
     * AI를 사용하여 프로젝트의 과제를 자동 생성합니다.
     */
    @PostMapping("/ai/generate/{projectId}")
    public ResponseEntity<?> generateAssignmentsWithAI(@PathVariable Long projectId) {
        if (aiAssignmentService.isEmpty()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body("AI 서비스가 사용할 수 없습니다. Gemini API 키를 설정해주세요.");
        }

        try {
            List<TeamAssignmentDto.Res> assignments = aiAssignmentService.get().generateAssignments(projectId);
            return ResponseEntity.ok(assignments);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("AI 과제 생성 중 오류가 발생했습니다: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * AI를 사용하여 프로젝트의 일정을 자동 생성합니다.
     */
    @PostMapping("/ai/schedule/{projectId}")
    public ResponseEntity<?> generateScheduleWithAI(@PathVariable Long projectId) {
        if (aiAssignmentService.isEmpty()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body("AI 서비스가 사용할 수 없습니다. Gemini API 키를 설정해주세요.");
        }

        try {
            List<TeamAssignmentDto.Res> schedules = aiAssignmentService.get().generateSchedule(projectId);
            return ResponseEntity.ok(schedules);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("AI 일정 생성 중 오류가 발생했습니다: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
