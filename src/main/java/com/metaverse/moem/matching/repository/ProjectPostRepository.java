package com.metaverse.moem.matching.repository;

import com.metaverse.moem.matching.domain.ProjectPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProjectPostRepository extends JpaRepository<ProjectPost, Long> {
    Optional<ProjectPost> findByTeam_Id(Long teamId);
    List<ProjectPost> findByIsDeletedFalse();
    @Query("SELECT tag FROM ProjectPost project JOIN project.tags tag WHERE project.id = :projectId")
    List<String> findTagsByProjectPostId(@Param("projectId") Long projectId);
}
