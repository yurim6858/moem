package com.metaverse.moem.project.repository;

import com.metaverse.moem.project.domain.Project;
import com.metaverse.moem.project.domain.ProjectType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface  ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findByTypeAndOwnerIdAndIsDeletedFalse(ProjectType type, Long ownerId);
}
