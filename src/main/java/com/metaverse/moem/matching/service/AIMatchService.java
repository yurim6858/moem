package com.metaverse.moem.matching.service;

import com.metaverse.moem.matching.domain.UserPost;
import com.metaverse.moem.matching.repository.UserPostRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // 💡 Transactional 추가

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@Profile({"default","mock"})
public class AIMatchService {

    private final UserPostRepository userPostRepository;
    private final GeminiService geminiService;

    public AIMatchService(UserPostRepository userPostRepository,
                          @Autowired(required = false) GeminiService geminiService) {
        this.userPostRepository = userPostRepository;
        this.geminiService = geminiService;
    }

    public List<UserPost> getAllUserPosts() {
        return userPostRepository.findAll();
    }

    public List<UserPost> recommendByTags(List<String> tags) {
        log.debug("태그로 사용자 검색: {}", tags);

        if (tags == null || tags.isEmpty()) {
            return getAllUserPosts();
        }

        Set<String> lowerCaseTags = tags.stream()
                .map(String::toLowerCase)
                .collect(Collectors.toSet());

        return userPostRepository.findAll().stream()
                .filter(userPost -> {
                    Set<String> userSkills = userPost.getSkills().stream()
                            .map(String::toLowerCase)
                            .collect(Collectors.toSet());
                    return userSkills.stream().anyMatch(lowerCaseTags::contains);
                })
                .toList();
    }

    @Transactional
    public String getAiRecommendationReason(Long userPostId) throws IOException {
        UserPost userPost = userPostRepository.findById(userPostId)
                .orElseThrow(() -> new NoSuchElementException("프로필 게시물을 찾을 수 없습니다: " + userPostId));

        String cachedSummary = userPost.getAiSummary();

        if (cachedSummary != null && !cachedSummary.isEmpty()) {
            log.info("AI Summary for Post ID {} loaded from cache (DB).", userPostId);
            return cachedSummary;
        }

        if (geminiService == null) {
            log.warn("GeminiService를 사용할 수 없습니다. 기본 메시지를 반환합니다.");
            return String.format("이 팀원은 %s 기술을 보유하고 있어 프로젝트에 도움이 될 것입니다.",
                    String.join(", ", userPost.getSkills()));
        }

        String prompt = String.format(
                "개발자 프로필: {이름: %s, 역할: %s, 보유 기술: %s, 소개: %s}. " +
                        "이 사람을 팀 프로젝트에 영입할 때의 핵심 장점 1가지를 '이 팀원은...'으로 시작하는 한 문장으로 요약해줘.",
                userPost.getUsername(),
                "개발자",
                String.join(", ", userPost.getSkills()),
                userPost.getIntro()
        );

        log.info("Gemini 추천 이유 생성 요청: {}", userPost.getUsername());
        String newSummary = geminiService.getCompletion(prompt);

        userPost.setAiSummary(newSummary);
        log.info("AI Summary for Post ID {} successfully generated and cached to DB.", userPostId);

        return newSummary;
    }
}