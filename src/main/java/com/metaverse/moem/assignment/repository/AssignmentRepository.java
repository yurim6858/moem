package com.metaverse.moem.assignment.repository;

import com.metaverse.moem.assignment.domain.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    List<Assignment> findAllByProjectId(Long projectId);
    List<Assignment> findAllByProjectIdAndUserId(Long projectId, Long userId);
}
