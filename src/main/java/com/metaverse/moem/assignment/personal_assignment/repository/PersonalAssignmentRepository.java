package com.metaverse.moem.assignment.personal_assignment.repository;

import com.metaverse.moem.assignment.personal_assignment.domain.PersonalAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PersonalAssignmentRepository extends JpaRepository<PersonalAssignment, Long> {

    List<PersonalAssignment> findAllByUserId(Long userId);
}
