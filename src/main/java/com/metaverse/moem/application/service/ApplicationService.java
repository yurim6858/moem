package com.metaverse.moem.application.service;

import com.metaverse.moem.application.domain.Application;
import com.metaverse.moem.application.dto.ApplicationRequest;
import com.metaverse.moem.application.dto.ApplicationResponse;
import com.metaverse.moem.application.repository.ApplicationRepository;
import com.metaverse.moem.auth.domain.Auth;
import com.metaverse.moem.auth.repository.AuthRepository;
import com.metaverse.moem.matching.domain.ProjectPost;
import com.metaverse.moem.matching.repository.ProjectPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final ProjectPostRepository projectPostRepository;
    private final AuthRepository authRepository;

    @Transactional
    public ApplicationResponse apply(ApplicationRequest request, String applicantUsername) {
        // 프로젝트 조회
        ProjectPost project = projectPostRepository.findById(request.getProjectId())
                .orElseThrow(() -> new RuntimeException("프로젝트를 찾을 수 없습니다."));

        // 지원자 조회 (Auth에서 직접 조회)
        Auth applicant = authRepository.findByUsername(applicantUsername)
                .orElseThrow(() -> new RuntimeException("지원자를 찾을 수 없습니다."));

        // 중복 지원 체크
        if (applicationRepository.existsByProjectAndApplicant(project, applicant)) {
            throw new RuntimeException("이미 지원한 프로젝트입니다.");
        }

        Application application = Application.builder()
                .project(project)
                .applicant(applicant)
                .message(request.getMessage())
                .status(Application.ApplicationStatus.PENDING)
                .build();

        Application savedApplication = applicationRepository.save(application);
        return convertToResponse(savedApplication);
    }

    public List<ApplicationResponse> getApplicationsByProject(Long projectId) {
        return applicationRepository.findByProject_Id(projectId).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<ApplicationResponse> getApplicationsByUser(String username) {
        // username으로 Auth를 찾고, 해당 Auth의 ID로 Application을 조회
        Auth applicant = authRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        return applicationRepository.findByApplicant(applicant).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public ApplicationResponse updateApplicationStatus(Long applicationId, Application.ApplicationStatus status) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("지원 정보를 찾을 수 없습니다."));

        application.setStatus(status);
        Application updatedApplication = applicationRepository.save(application);
        return convertToResponse(updatedApplication);
    }

    @Transactional
    public void withdrawApplication(Long applicationId, String username) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("지원 정보를 찾을 수 없습니다."));

        // 지원자 확인
        Auth applicant = authRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        if (!application.isFromSameUser(applicant)) {
            throw new RuntimeException("본인의 지원만 취소할 수 있습니다.");
        }

        application.setStatus(Application.ApplicationStatus.WITHDRAWN);
        applicationRepository.save(application);
    }

    private ApplicationResponse convertToResponse(Application application) {
        ApplicationResponse response = new ApplicationResponse();
        response.setId(application.getId());
        response.setProjectId(application.getProjectId());
        response.setApplicantUsername(application.getApplicantUsername());
        response.setMessage(application.getMessage());
        response.setStatus(application.getStatus());
        response.setAppliedAt(application.getAppliedAt());
        response.setUpdatedAt(application.getUpdatedAt());
        return response;
    }
}

