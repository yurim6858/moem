package com.metaverse.moem.matching.service;

import com.metaverse.moem.gemini.service.GeminiService;
import com.metaverse.moem.matching.domain.MatchRecommendationCache;
import com.metaverse.moem.matching.domain.ProjectPost;
import com.metaverse.moem.matching.domain.UserPost;
import com.metaverse.moem.matching.repository.MatchRecommendationCacheRepository;
import com.metaverse.moem.matching.repository.ProjectPostRepository;
import com.metaverse.moem.matching.repository.UserPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectMatchServiceImplements implements ProjectMatchService {

    private final UserPostRepository userPostRepository;
    private final ProjectPostRepository projectPostRepository;
    private final MatchRecommendationCacheRepository cacheRepository;
    private final GeminiService geminiService;

    private static final String SYSTEM_PROMPT = """
            당신은 최고의 프로젝트/인력 매칭 전문가입니다.
            주어진 사용자 프로필(Seeker)과 프로젝트 정보(Project)를 분석하여,
            Seeker가 Project에 적합한 이유를 50자 내외의 한국어로 간결하게 요약하여 한 문장만 응답하세요.
            응답은 오직 요약된 문장 하나여야 하며, 다른 부가적인 설명은 절대 포함하지 마세요.
            예시: "이 프로젝트는 Spring에 대한 전문적인 백엔드 개발자를 필요로하여 당신의 Spring 개발경험과 경력이 큰 도움이 될 수 있습니다."
            """;

    @Override
    @Transactional // 쓰기(캐시 저장)와 읽기 모두 처리
    public String getMatchReasonForUser(Long userId, Long projectId) {

        UserPost seeker = userPostRepository.findByAuth_Id(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 프로필을 찾을 수 없습니다: " + userId));

        ProjectPost project = projectPostRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("프로젝트를 찾을 수 없습니다: " + projectId));

        Optional<MatchRecommendationCache> cachedResult = cacheRepository.findByUserPostAndProjectPost(seeker, project);

        if (cachedResult.isPresent()) {
            return cachedResult.get().getReasonForProjectSeeker();
        } else {

            try {
                String seekerInfo = formatUserPostForAI(seeker);
                String projectInfo = formatProjectPostForAI(project);
                String prompt = "Seeker Profile:\n" + seekerInfo + "\n\nProject Details:\n" + projectInfo;

                // 🔥 동기 호출 (API Latency 예상)
                String matchReason = geminiService.generateContent(SYSTEM_PROMPT, prompt);

                // 3. 캐시 저장
                MatchRecommendationCache newCache = new MatchRecommendationCache(
                        null, seeker, project, null, matchReason, LocalDateTime.now(), LocalDateTime.now()
                );
                cacheRepository.save(newCache);

                System.out.println("✅ AI On-Demand Success: User ID " + userId + " matched with Project ID " + projectId);
                return matchReason;

            } catch (Exception e) {
                System.err.println("❌ Gemini On-Demand 오류: " + e.getMessage());
                return "AI 추천 이유 생성 중 오류가 발생했습니다: " + e.getMessage();
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectPost> getRecommendedProjects(Long userId, int limit) {
        UserPost seeker = userPostRepository.findByAuth_Id(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 프로필을 찾을 수 없습니다: " + userId));

        // Note: 이 메서드는 현재 매칭 점수가 없으므로, 단순히 캐시된 프로젝트를 나열합니다.
        List<MatchRecommendationCache> recommendations =
                cacheRepository.findByUserPost(seeker);

        return recommendations.stream()
                .map(MatchRecommendationCache::getProjectPost)
                .limit(limit)
                .collect(Collectors.toList());
    }

    private String formatUserPostForAI(UserPost userPost) {
        String skills = userPost.getSkills().stream().collect(Collectors.joining(", "));
        return String.format("Intro: %s | Work Style: %s | Contact: %s | Skills: %s | Collaboration Period: %s",
                userPost.getIntro(), userPost.getWorkStyle(), userPost.getContactType(), skills, userPost.getCollaborationPeriod());
    }

    private String formatProjectPostForAI(ProjectPost projectPost) {
        String tags = projectPostRepository.findTagsByProjectPostId(projectPost.getId()).stream().collect(Collectors.joining(", "));

        return String.format("Title: %s | Intro: %s | Period: %s | Required Tags: %s",
                projectPost.getTitle(), projectPost.getIntro(), projectPost.getCollaborationPeriod(), tags);
    }
}