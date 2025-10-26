package com.metaverse.moem.matching.service;

import com.metaverse.moem.matching.domain.User;
import com.metaverse.moem.matching.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Profile({"default","mock"})
public class AIMatchService {

    private final UserRepository userRepository;
    private final GeminiService geminiService;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<User> recommendByTags(List<String> tags) {
        log.debug("태그로 사용자 검색: {}", tags);

        if (tags == null || tags.isEmpty()) {
            return getAllUsers();
        }

        Set<String> lowerCaseTags = tags.stream()
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        return userRepository.findAll().stream()
                .filter(user -> {
                    Set<String> userSkills = user.getSkills().stream()
                            .map(String::toLowerCase)
                            .collect(Collectors.toSet());
                    return userSkills.stream().anyMatch(lowerCaseTags::contains);
                })
                .toList();
    }

    public String getAiRecommendationReason(Long userId) throws IOException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + userId));

        String prompt = String.format(
                "개발자 프로필: {이름: %s, 역할: %s, 보유 기술: %s, 소개: %s}. " +
                        "이 사람을 팀 프로젝트에 영입할 때의 핵심 장점 1가지를 '이 팀원은...'으로 시작하는 한 문장으로 요약해줘.",
                user.getName(),
                user.getRole(),
                String.join(", ", user.getSkills()),
                user.getNote()
        );

        log.info("Gemini 추천 이유 생성 요청: {}", user.getName());
        return geminiService.getCompletion(prompt);
    }
}