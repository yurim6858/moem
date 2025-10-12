package com.metaverse.moem.application.repository;

import com.metaverse.moem.application.domain.Application;
import com.metaverse.moem.auth.domain.Auth;
import com.metaverse.moem.matching.domain.ProjectPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {
    List<Application> findByProject(ProjectPost project);
    List<Application> findByApplicant(Auth applicant);
    Optional<Application> findByProjectAndApplicant(ProjectPost project, Auth applicant);
    boolean existsByProjectAndApplicant(ProjectPost project, Auth applicant);
    
    // 연관관계를 통한 조회 (중첩 속성 사용)
    List<Application> findByProject_Id(Long projectId);
    List<Application> findByApplicant_Id(Long applicantId);
    Optional<Application> findByProject_IdAndApplicant_Id(Long projectId, Long applicantId);
    boolean existsByProject_IdAndApplicant_Id(Long projectId, Long applicantId);
}

