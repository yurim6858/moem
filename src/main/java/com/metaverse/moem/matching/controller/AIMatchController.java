package com.metaverse.moem.matching.controller;

import com.metaverse.moem.matching.domain.PreferenceRecommendRequest;
import com.metaverse.moem.matching.dto.RecommendationResponseDto;
import com.metaverse.moem.matching.service.AIMatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/recommend/by-preference")
    public RecommendationResponseDto recommendByPreference(@RequestBody PreferenceRecommendRequest preferenceRecommendRequest) {
        return aiMatchService.recommendByPreference(preferenceRecommendRequest);
    }
}
