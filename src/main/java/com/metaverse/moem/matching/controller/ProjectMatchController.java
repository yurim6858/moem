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
        List<ProjectPost> recommendedProjects = projectMatchService.getRecommendedProjects(userId, 5);

        return ResponseEntity.ok(recommendedProjects);
    }

    @GetMapping("/reason/{userId}/{projectId}")
    public ResponseEntity<String> getMatchReason(
            @PathVariable Long userId,
            @PathVariable Long projectId
    ) {

        String reason = projectMatchService.getMatchReasonForUser(userId, projectId);
        return ResponseEntity.ok(reason);
    }

}