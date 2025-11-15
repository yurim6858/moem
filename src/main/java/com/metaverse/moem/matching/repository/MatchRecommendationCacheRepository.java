package com.metaverse.moem.matching.repository;

import com.metaverse.moem.matching.domain.MatchRecommendationCache;
import com.metaverse.moem.matching.domain.ProjectPost;
import com.metaverse.moem.matching.domain.UserPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MatchRecommendationCacheRepository extends JpaRepository<MatchRecommendationCache, Long> {
    Optional<MatchRecommendationCache> findByUserPostAndProjectPost(UserPost userPost, ProjectPost projectPost);
    List<MatchRecommendationCache> findByUserPost(UserPost userPost);
}