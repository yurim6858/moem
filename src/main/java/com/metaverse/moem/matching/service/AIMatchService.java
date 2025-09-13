package com.metaverse.moem.matching.service;

import com.metaverse.moem.matching.dto.RecommendationRequestDto;
import com.metaverse.moem.matching.dto.RecommendationResponseDto;
import com.metaverse.moem.matching.domain.User;
import com.metaverse.moem.matching.dto.UserDto;
import com.metaverse.moem.matching.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Profile({"default","mock"})
public class AIMatchService {

    private final UserRepository userRepository;

    public RecommendationResponseDto recommend(Long baseUserId, int limit) {
        User base = userRepository.findById(baseUserId)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + baseUserId));

        Set<String> baseTokens = tokens(base);

        List<RecommendationRequestDto> recommendation = userRepository.findAll().stream()
                .filter(user -> !Objects.equals(user.getId(), base.getId()))
                .map(user -> {
                    double score = jaccard(baseTokens, tokens(user));
                    return RecommendationRequestDto.builder()
                            .userId(user.getId())
                            .name(user.getName())
                            .skills(user.getSkills())
                            .interests(user.getInterests())
                            .score(round(score))
                            .build();
                })
                .sorted(Comparator.comparing(RecommendationRequestDto::getScore).reversed())
                .limit(limit)
                .collect(Collectors.toList());

        UserDto baseDto = UserDto.builder()
                .userId(base.getId())
                .name(base.getName())
                .skills(base.getSkills())
                .interests(base.getInterests())
                .build();

        return RecommendationResponseDto.builder()
                .baseUser(baseDto)
                .recommendations(recommendation)
                .build();

    }

    private Set<String> tokens(User user) {
        Set<String> tokens = new HashSet<>();
        if (user.getSkills() != null) tokens
                .addAll(user.getSkills().stream().map(String::toLowerCase).toList());
        if (user.getInterests() != null) tokens
                .addAll(user.getInterests().stream().map(String::toLowerCase).toList());
        return tokens;
    }

    private Double jaccard(Set<String> baseTokens, Set<String> tokens) {
        if (baseTokens.isEmpty() && tokens.isEmpty()) return 0.0;
        Set<String> intersection = new HashSet<>(baseTokens);
        intersection.retainAll(tokens);
        Set<String> union = new HashSet<>(baseTokens);
        union.addAll(tokens);
        return (double) intersection.size() / (double) union.size();
    }

    private double round(double value) {
        return Math.round(value * 1000.0) / 1000.0;
    }
}
