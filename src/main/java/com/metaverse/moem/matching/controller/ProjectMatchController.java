package com.metaverse.moem.matching.controller;

import com.metaverse.moem.matching.domain.ProjectPost;
import com.metaverse.moem.matching.service.ProjectMatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/project-match")
@RequiredArgsConstructor
public class ProjectMatchController {

    private final ProjectMatchService projectMatchService;

    @GetMapping("/recommendations/{userId}")
    public ResponseEntity<List<ProjectPost>> getRecommendedProjects(@PathVariable Long userId) {
        try {
            List<ProjectPost> recommendedProjects = projectMatchService.getRecommendedProjects(userId, 5);
            return ResponseEntity.ok(recommendedProjects);
        } catch (IllegalArgumentException e) {
            // 사용자 프로필이 없는 경우 404 반환 (빈 리스트)
            if (e.getMessage() != null && e.getMessage().contains("사용자 프로필을 찾을 수 없습니다")) {
                return ResponseEntity.status(404).body(List.of());
            }
            // 기타 IllegalArgumentException은 400 Bad Request (빈 리스트)
            return ResponseEntity.badRequest().body(List.of());
        } catch (Exception e) {
            // 기타 예외는 500 Internal Server Error (빈 리스트)
            return ResponseEntity.internalServerError().body(List.of());
        }
    }

    @GetMapping("/reason/{userId}/{projectId}")
    public ResponseEntity<String> getMatchReason(
            @PathVariable Long userId,
            @PathVariable Long projectId
    ) {
        try {
            String reason = projectMatchService.getMatchReasonForUser(userId, projectId);
            return ResponseEntity.ok(reason);
        } catch (IllegalArgumentException e) {
            // 사용자 프로필이 없는 경우 404 반환
            if (e.getMessage() != null && e.getMessage().contains("사용자 프로필을 찾을 수 없습니다")) {
                return ResponseEntity.status(404).body("사용자 프로필을 찾을 수 없습니다. 프로필을 먼저 작성해주세요.");
            }
            // 프로젝트를 찾을 수 없는 경우도 404 반환
            if (e.getMessage() != null && e.getMessage().contains("프로젝트를 찾을 수 없습니다")) {
                return ResponseEntity.status(404).body("프로젝트를 찾을 수 없습니다.");
            }
            // 기타 IllegalArgumentException은 400 Bad Request
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            // 기타 예외는 500 Internal Server Error
            return ResponseEntity.internalServerError().body("매칭 이유 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

}