package com.metaverse.moem.matching.controller;

import com.metaverse.moem.matching.domain.PreferenceRecommendRequest;
import com.metaverse.moem.matching.domain.User;
import com.metaverse.moem.matching.dto.RecommendationResponseDto;
import com.metaverse.moem.matching.service.AIMatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/match")
@RequiredArgsConstructor
public class AIMatchController {

    private final AIMatchService aiMatchService;

    @GetMapping("/recommend")
    public RecommendationResponseDto recommend(
            @RequestParam Long baseUserId,
            @RequestParam(name = "limit", required = false, defaultValue = "5") Integer limit) {
        return aiMatchService.recommend(baseUserId, Math.max(1, Math.min(limit, 20)));
    }

    // ✨ 이 부분을 수정했습니다.
    @GetMapping("/users")
    public List<User> getAllUsers() {
        return aiMatchService.getAllUsers();
    }

    @PostMapping("/recommend/by-preference")
    public RecommendationResponseDto recommendByPreference(@RequestBody PreferenceRecommendRequest preferenceRecommendRequest) {
        return aiMatchService.recommendByPreference(preferenceRecommendRequest);
    }
}