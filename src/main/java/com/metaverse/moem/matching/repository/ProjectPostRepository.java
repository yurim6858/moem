package com.metaverse.moem.matching.repository;

import com.metaverse.moem.matching.domain.ProjectPost;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ProjectPostRepository extends JpaRepository<ProjectPost, Long> {
    Optional<ProjectPost> findByTeam_Id(Long teamId);
    List<ProjectPost> findByIsDeletedFalse();
}
