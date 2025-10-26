package com.metaverse.moem.matching.controller;

import com.metaverse.moem.matching.domain.User;
import com.metaverse.moem.matching.service.AIMatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/match")
@RequiredArgsConstructor
public class AIMatchController {

    private final AIMatchService aiMatchService;

    @GetMapping("/users")
    public List<User> getAllUsers() {
        return aiMatchService.getAllUsers();
    }

    @PostMapping("/tags")
    public List<User> getRecommendByTags(@RequestBody List<String> tags) {
        return aiMatchService.recommendByTags(tags);
    }

    @GetMapping("/reason")
    public Map<String, String> getAiReason(@RequestParam Long userId) throws IOException {
        return Map.of("reason", aiMatchService.getAiRecommendationReason(userId));
    }
}