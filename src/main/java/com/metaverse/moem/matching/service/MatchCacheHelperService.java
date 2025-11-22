package com.metaverse.moem.matching.service;

import com.metaverse.moem.matching.domain.MatchRecommendationCache;
import com.metaverse.moem.matching.domain.ProjectPost;
import com.metaverse.moem.matching.domain.UserPost;
import com.metaverse.moem.matching.repository.MatchRecommendationCacheRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MatchCacheHelperService {

    private final MatchRecommendationCacheRepository cacheRepository;

    // AI 호출 후 저장 및 동시성 처리
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public String saveAndHandleConcurrency(UserPost seeker, ProjectPost project, String matchReason) {

        MatchRecommendationCache newCache = new MatchRecommendationCache(
                null, seeker, project, null, matchReason, LocalDateTime.now(), LocalDateTime.now()
        );

        try {
            cacheRepository.save(newCache);
            return matchReason;
        } catch (DataIntegrityViolationException e) {
            // Duplicate entry 오류 발생 시: 새 트랜잭션에서 재조회 호출
            return recheckCacheInNewTransaction(seeker.getId(), project.getId());
        }
    }

    // 재조회 로직 (완전히 새로운 읽기 전용 트랜잭션)
    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    public String recheckCacheInNewTransaction(Long userPostId, Long projectId) {
        Optional<MatchRecommendationCache> reCheckedCache =
                cacheRepository.findByUserPostIdAndProjectId(userPostId, projectId);

        if (reCheckedCache.isPresent()) {
            return reCheckedCache.get().getReasonForProjectSeeker();
        } else {
            // 재조회도 실패하면 심각한 오류입니다.
            throw new IllegalStateException("캐시 저장 오류 후 재조회 실패. DB 상태 확인 필요.");
        }
    }
}