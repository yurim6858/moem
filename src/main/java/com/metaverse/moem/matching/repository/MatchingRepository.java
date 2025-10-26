package com.metaverse.moem.matching.repository;

import com.metaverse.moem.matching.domain.Matching;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MatchingRepository extends JpaRepository<Matching, Long> {
    List<Matching> findByTagsContaining(String tag);
}
