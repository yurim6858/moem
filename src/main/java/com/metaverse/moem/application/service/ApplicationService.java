package com.metaverse.moem.application.service;

import com.metaverse.moem.application.domain.Application;
import com.metaverse.moem.application.dto.ApplicationRequest;
import com.metaverse.moem.application.dto.ApplicationResponse;
import com.metaverse.moem.application.repository.ApplicationRepository;
import com.metaverse.moem.auth.domain.User;
import com.metaverse.moem.auth.repository.UserRepository;
import com.metaverse.moem.matching.domain.ProjectPost;
import com.metaverse.moem.matching.repository.ProjectPostRepository;
import com.metaverse.moem.team.domain.Team;
import com.metaverse.moem.team.dto.TeamInvitationDto;
import com.metaverse.moem.team.service.TeamInvitationService;
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
    private final UserRepository userRepository;
    private final TeamInvitationService teamInvitationService;

    @Transactional
    public ApplicationResponse apply(ApplicationRequest request, String applicantUsername) {
        // 프로젝트 조회
        ProjectPost project = projectPostRepository.findById(request.getProjectId())
                .orElseThrow(() -> new RuntimeException("프로젝트를 찾을 수 없습니다."));

        // 지원자 조회 (Auth에서 직접 조회)
        User applicant = userRepository.findByUsername(applicantUsername)
                .orElseThrow(() -> new RuntimeException("지원자를 찾을 수 없습니다."));

        // 중복 지원 체크
        if (applicationRepository.existsByProjectAndApplicant(project, applicant)) {
            throw new RuntimeException("이미 지원한 프로젝트입니다.");
        }

        Application application = Application.builder()
                .project(project)
                .applicant(applicant)
                .message(request.getMessage())
                .appliedPosition(request.getAppliedPosition())
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
        User applicant = userRepository.findByUsername(username)
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
    public ApplicationResponse approveAndSendInvitation(Long applicationId, String approverUsername) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("지원 정보를 찾을 수 없습니다."));

        // 승인 권한 확인
        User approver = userRepository.findByUsername(approverUsername)
                .orElseThrow(() -> new RuntimeException("승인자를 찾을 수 없습니다."));

        if (!application.canBeApprovedBy(approver)) {
            throw new RuntimeException("승인 권한이 없습니다.");
        }

        // 지원서 승인
        application.setStatus(Application.ApplicationStatus.APPROVED);
        applicationRepository.save(application);

        // 팀 초대 발송
        sendTeamInvitation(application, approverUsername);

        return convertToResponse(application);
    }

    private void sendTeamInvitation(Application application, String approverUsername) {
        try {
            // 프로젝트에 연결된 팀 찾기 또는 생성
            Team team = findOrCreateTeamForProject(application.getProject());

            // 팀 초대 발송 (포지션 정보 포함)
            String invitationMessage = String.format(
                "프로젝트 지원이 승인되어 팀에 초대드립니다. 포지션: %s. 초대를 수락하시면 팀 멤버가 됩니다.",
                application.getAppliedPosition() != null ? application.getAppliedPosition() : "미지정"
            );
            
            TeamInvitationDto.CreateReq createReq = new TeamInvitationDto.CreateReq(
                    application.getApplicant().getId(),
                    invitationMessage
            );

            teamInvitationService.sendInvitation(team.getId(), createReq, approverUsername);
        } catch (Exception e) {
            // 초대 발송 실패 시 지원 상태 롤백
            application.setStatus(Application.ApplicationStatus.PENDING);
            applicationRepository.save(application);
            throw new RuntimeException("팀 초대 발송에 실패했습니다: " + e.getMessage());
        }
    }

    private Team findOrCreateTeamForProject(ProjectPost project) {
        // 프로젝트 생성 시점에 이미 팀이 생성되어 있어야 함
        if (project.getTeam() != null) {
            return project.getTeam();
        }

        // 팀이 없다면 예외 발생 (정상적인 경우가 아님)
        throw new RuntimeException("프로젝트에 연결된 팀을 찾을 수 없습니다. 프로젝트를 다시 생성해주세요.");
    }

    @Transactional
    public void withdrawApplication(Long applicationId, String username) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("지원 정보를 찾을 수 없습니다."));

        // 지원자 확인
        User applicant = userRepository.findByUsername(username)
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
        response.setAppliedPosition(application.getAppliedPosition());
        response.setStatus(application.getStatus());
        response.setAppliedAt(application.getAppliedAt());
        response.setUpdatedAt(application.getUpdatedAt());
        return response;
    }
}

