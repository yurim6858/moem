package com.metaverse.moem.project.repository;

import com.metaverse.moem.project.domain.Project;
import com.metaverse.moem.project.domain.ProjectType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project, Long> {

    Optional<Project> findByIdAndIsDeletedFalse(Long id);

    @Query("""
        select project from Project project
        where project.isDeleted = false
          and (:type is null or project.type = :type)
          and (
              :status is null
              or (:status = 'OPEN' and (project.recruitEndDate is null or project.recruitEndDate >= current_date))
              or (:status = 'CLOSED' and project.recruitEndDate is not null and project.recruitEndDate < current_date)
          )
          and (
              :query is null
              or lower(project.name) like lower(concat('%', :query, '%'))
              or lower(project.description) like lower(concat('%', :query, '%'))
          )
        order by project.createdAt desc
    """)
    List<Project> searchPublic(
            @Param("type") ProjectType type,
            @Param("status") String status,
            @Param("query") String query
    );

    @Query("""
        select project from Project project
        where project.isDeleted = false
          and (
              project.projectEndDate is null
              or project.projectEndDate >= current_date
          )
    """)
    List<Project> findActiveProjects();
}


