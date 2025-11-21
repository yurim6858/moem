package com.metaverse.moem.matching.service;

import com.metaverse.moem.gemini.service.GeminiService;
import com.metaverse.moem.matching.domain.MatchRecommendationCache;
import com.metaverse.moem.matching.domain.ProjectPost;
import com.metaverse.moem.matching.domain.UserPost;
import com.metaverse.moem.matching.repository.MatchRecommendationCacheRepository;
import com.metaverse.moem.matching.repository.ProjectPostRepository;
import com.metaverse.moem.matching.repository.UserPostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProjectMatchServiceImplements implements ProjectMatchService {

    private final UserPostRepository userPostRepository;
    private final ProjectPostRepository projectPostRepository;
    private final MatchRecommendationCacheRepository cacheRepository;
    private final Optional<GeminiService> geminiService;

    public ProjectMatchServiceImplements(
            UserPostRepository userPostRepository,
            ProjectPostRepository projectPostRepository,
            MatchRecommendationCacheRepository cacheRepository,
            Optional<GeminiService> geminiService) {
        this.userPostRepository = userPostRepository;
        this.projectPostRepository = projectPostRepository;
        this.cacheRepository = cacheRepository;
        this.geminiService = geminiService;
    }

    private static final String SYSTEM_PROMPT = """
            당신은 최고의 프로젝트/인력 매칭 전문가입니다.
            주어진 사용자 프로필(Seeker)과 프로젝트 정보(Project)를 분석하여,
            Seeker가 Project에 적합한 이유를 50자 내외의 한국어로 간결하게 요약하여 한 문장만 응답하세요.
            응답은 오직 요약된 문장 하나여야 하며, 다른 부가적인 설명은 절대 포함하지 마세요.
            예시: "이 프로젝트는 Spring에 대한 전문적인 백엔드 개발자를 필요로하여 당신의 Spring 개발경험과 경력이 큰 도움이 될 수 있습니다."
            """;

    @Override
    @Transactional
    public String getMatchReasonForUser(Long userId, Long projectId) {

        // 1. Auth ID (API Path의 userId)로 UserPost 엔티티를 조회합니다.
        UserPost seeker = userPostRepository.findByAuth_Id(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 프로필을 찾을 수 없습니다: " + userId));

        // 2. 🔑 캐시 조회: UserPost의 Primary Key를 사용 (개인화 강제)
        Long userPostId = seeker.getId(); // UserPost의 PK (예: A의 1051, B의 1052)

        // 명시적인 PK 기반 쿼리를 사용하여, B가 A의 캐시를 조회하는 상황을 방지합니다.
        Optional<MatchRecommendationCache> cachedResult =
                cacheRepository.findByUserPostIdAndProjectId(userPostId, projectId);

        if (cachedResult.isPresent()) {
            // ✅ B 사용자의 요약본 (K)이 존재하면 반환
            return cachedResult.get().getReasonForProjectSeeker();
        } else {
            // ❌ B 사용자의 요약본이 존재하지 않으면 (AI 호출 및 저장)

            ProjectPost project = projectPostRepository.findById(projectId)
                    .orElseThrow(() -> new IllegalArgumentException("프로젝트를 찾을 수 없습니다: " + projectId));

            try {
                // ... (AI 호출 로직은 동일)
                String seekerInfo = formatUserPostForAI(seeker);
                String projectInfo = formatProjectPostForAI(project);
                String prompt = "Seeker Profile:\n" + seekerInfo + "\n\nProject Details:\n" + projectInfo;

                String matchReason = geminiService
                        .map(service -> {
                            try {
                                return service.generateContent(SYSTEM_PROMPT, prompt);
                            } catch (Exception e) {
                                throw new RuntimeException("Gemini API 호출 실패", e);
                            }
                        })
                        .orElse("이 프로젝트는 당신의 기술 스택과 경력이 잘 맞아 보입니다.");

                // 3. 캐시 저장: 새로 생성된 요약본을 정확한 엔티티와 함께 저장 (K 생성)
                // @PrePersist가 createdAt과 updatedAt을 자동 설정하므로 null로 설정
                MatchRecommendationCache newCache = new MatchRecommendationCache();
                newCache.setUserPost(seeker);
                newCache.setProjectPost(project);
                newCache.setReasonForProjectSeeker(matchReason);
                // reasonForProjectOwner는 null로 유지 (필요시 나중에 설정)
                // createdAt과 updatedAt은 @PrePersist에서 자동 설정됨
                cacheRepository.save(newCache);

                System.out.println("✅ AI On-Demand Success: User ID " + userId + " matched with Project ID " + projectId);
                return matchReason;

            } catch (Exception e) {
                // 이 오류는 주로 Duplicate Entry 오류일 가능성이 높습니다.
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