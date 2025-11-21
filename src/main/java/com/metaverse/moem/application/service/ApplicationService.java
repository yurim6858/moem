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
import com.metaverse.moem.team.domain.TeamMembers;
import com.metaverse.moem.team.repository.TeamMembersRepository;
import com.metaverse.moem.team.service.TeamMembersService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final ProjectPostRepository projectPostRepository;
    private final UserRepository userRepository;
    private final TeamMembersService teamMembersService;
    private final TeamMembersRepository teamMembersRepository;

    @Transactional
    public ApplicationResponse apply(ApplicationRequest request, String applicantUsername) {
        // 프로젝트 조회
        ProjectPost project = projectPostRepository.findById(request.getProjectId())
                .orElseThrow(() -> new RuntimeException("프로젝트를 찾을 수 없습니다."));

        // 지원자 조회 (Auth에서 직접 조회)
        User applicant = userRepository.findByUsername(applicantUsername)
                .orElseThrow(() -> new RuntimeException("지원자를 찾을 수 없습니다."));

        // 기존 지원 내역 확인 (재지원 가능 여부 판단)
        java.util.Optional<Application> existingApplicationOpt =
                applicationRepository.findByProjectAndApplicant(project, applicant);

        if (existingApplicationOpt.isPresent()) {
            Application existingApplication = existingApplicationOpt.get();
            if (existingApplication.getStatus() != Application.ApplicationStatus.WITHDRAWN) {
                throw new RuntimeException("이미 지원한 프로젝트입니다.");
            }
        }

        // 포지션별 모집완료 체크
        if (request.getAppliedPosition() != null && !request.getAppliedPosition().trim().isEmpty()) {
            checkPositionRecruitmentStatus(project, request.getAppliedPosition());
        }

        // 기존 지원이 WITHDRAWN 상태라면 재사용, 아니면 새로 생성
        Application application;
        if (existingApplicationOpt.isPresent()) {
            application = existingApplicationOpt.get();
            application.setMessage(request.getMessage());
            application.setAppliedPosition(request.getAppliedPosition());
            application.setStatus(Application.ApplicationStatus.PENDING);
            application.setAppliedAt(java.time.LocalDateTime.now());
        } else {
            application = Application.builder()
                    .project(project)
                    .applicant(applicant)
                    .message(request.getMessage())
                    .appliedPosition(request.getAppliedPosition())
                    .status(Application.ApplicationStatus.PENDING)
                    .build();
        }

        Application savedApplication = applicationRepository.save(application);
        return convertToResponse(savedApplication);
    }

    // 포지션별 모집완료 여부 체크
    private void checkPositionRecruitmentStatus(ProjectPost project, String appliedPosition) {
        // 프로젝트의 포지션 목록 확인
        List<ProjectPost.Position> positions = project.getPositions();
        if (positions == null || positions.isEmpty()) {
            return; // 포지션이 없으면 체크하지 않음
        }

        // 해당 포지션 찾기
        ProjectPost.Position targetPosition = positions.stream()
                .filter(pos -> pos.getRole() != null && pos.getRole().equals(appliedPosition))
                .findFirst()
                .orElse(null);

        if (targetPosition == null) {
            throw new RuntimeException("해당 포지션이 프로젝트에 존재하지 않습니다: " + appliedPosition);
        }

        Integer required = targetPosition.getHeadcount() != null ? targetPosition.getHeadcount() : 0;
        if (required <= 0) {
            return; // 필요 인원이 없으면 체크하지 않음
        }

        // 해당 포지션의 승인된 지원자 수 확인
        List<Application> approvedApplications = applicationRepository.findByProject_Id(project.getId())
                .stream()
                .filter(app -> app.getStatus() == Application.ApplicationStatus.APPROVED)
                .filter(app -> appliedPosition.equals(app.getAppliedPosition()))
                .collect(Collectors.toList());

        int current = approvedApplications.size();
        
        // 모집완료된 포지션은 지원 불가
        if (current >= required) {
            throw new RuntimeException("해당 포지션의 모집이 완료되었습니다. (" + current + "/" + required + "명)");
        }
    }

    public List<ApplicationResponse> getApplicationsByProject(Long projectId) {
        return applicationRepository.findByProject_Id(projectId).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // 포지션별로 그룹화된 지원자 목록 조회
    public com.metaverse.moem.application.dto.ApplicationByPositionResponse.Res getApplicationsByProjectGroupedByPosition(Long projectId) {
        // 프로젝트 조회
        ProjectPost project = projectPostRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("프로젝트를 찾을 수 없습니다."));

        // 모든 지원서 조회
        List<Application> allApplications = applicationRepository.findByProject_Id(projectId);
        
        // 프로젝트의 포지션 목록 가져오기
        List<ProjectPost.Position> positions = project.getPositions();
        if (positions == null || positions.isEmpty()) {
            return com.metaverse.moem.application.dto.ApplicationByPositionResponse.Res.builder()
                    .projectId(projectId)
                    .positions(java.util.Collections.emptyList())
                    .allPositionsCompleted(false)
                    .canStartProject(false)
                    .build();
        }

        // 실제 팀 멤버 ID 목록 가져오기 (팀이 있는 경우)
        final java.util.List<Long> teamMemberIds;
        if (project.getTeam() != null) {
            teamMemberIds = teamMembersRepository.findByTeamId(project.getTeam().getId())
                    .stream()
                    .map(TeamMembers::getUserId)
                    .collect(Collectors.toList());
        } else {
            teamMemberIds = new java.util.ArrayList<>();
        }

        // 포지션별로 지원자 그룹화
        List<com.metaverse.moem.application.dto.ApplicationByPositionResponse.PositionApplications> positionApplicationsList = 
            new java.util.ArrayList<>();

        // 포지션별 승인된 지원자 수 카운트 (실제 팀 멤버만 카운트)
        Map<String, Long> approvedCountMap = allApplications.stream()
                .filter(app -> app.getStatus() == Application.ApplicationStatus.APPROVED)
                .filter(app -> {
                    // 실제 팀 멤버인지 확인 (팀이 없는 경우는 모든 APPROVED 상태 카운트)
                    if (teamMemberIds.isEmpty()) {
                        return true; // 팀이 없으면 모든 APPROVED 상태 카운트
                    }
                    try {
                        Long applicantId = app.getApplicantId();
                        return applicantId != null && teamMemberIds.contains(applicantId);
                    } catch (Exception e) {
                        return false;
                    }
                })
                .filter(app -> app.getAppliedPosition() != null && !app.getAppliedPosition().trim().isEmpty())
                .collect(Collectors.groupingBy(
                        Application::getAppliedPosition,
                        Collectors.counting()
                ));

        boolean allCompleted = true;
        
        for (ProjectPost.Position position : positions) {
            String role = position.getRole();
            Integer required = position.getHeadcount() != null ? position.getHeadcount() : 0;
            
            // 해당 포지션의 모든 지원자 필터링
            List<ApplicationResponse> positionApplications = allApplications.stream()
                    .filter(app -> role != null && role.equals(app.getAppliedPosition()))
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());

            // 승인된 인원 수
            Long approvedCount = approvedCountMap.getOrDefault(role, 0L);
            int current = approvedCount.intValue();
            
            // 모집완료 여부 확인
            boolean isRecruitmentCompleted = current >= required && required > 0;
            
            if (!isRecruitmentCompleted) {
                allCompleted = false;
            }

            positionApplicationsList.add(
                com.metaverse.moem.application.dto.ApplicationByPositionResponse.PositionApplications.builder()
                        .position(role != null ? role : "미지정")
                        .required(required)
                        .current(current)
                        .isRecruitmentCompleted(isRecruitmentCompleted)
                        .applications(positionApplications)
                        .build()
            );
        }

        return com.metaverse.moem.application.dto.ApplicationByPositionResponse.Res.builder()
                .projectId(projectId)
                .positions(positionApplicationsList)
                .allPositionsCompleted(allCompleted)
                .canStartProject(allCompleted)
                .build();
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

        Application.ApplicationStatus previousStatus = application.getStatus();
        application.setStatus(status);
        Application updatedApplication = applicationRepository.save(application);
        
        // APPROVED로 변경된 경우 자동으로 팀 멤버 추가
        if (status == Application.ApplicationStatus.APPROVED && 
            previousStatus != Application.ApplicationStatus.APPROVED) {
            // 이미 팀 멤버인지 확인
            try {
                addToTeam(application);
            } catch (Exception e) {
                // 이미 팀 멤버인 경우 등은 무시하고 계속 진행
                // (중복 추가 시도는 TeamMembersService에서 예외 발생)
            }
            
            // 포지션이 모집완료되었는지 확인하고, 완료되었다면 나머지 PENDING 지원자들을 WITHDRAWN 처리
            try {
                checkAndWithdrawPendingApplications(application);
            } catch (Exception e) {
                // 모집완료 체크 실패 시에도 계속 진행 (로깅만 하거나 무시)
            }
        }
        
        return convertToResponse(updatedApplication);
    }

    @Transactional
    public ApplicationResponse approveAndAddToTeam(Long applicationId, String approverUsername) {
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

        // 바로 팀 멤버로 추가
        addToTeam(application);
        
        // 포지션이 모집완료되었는지 확인하고, 완료되었다면 나머지 PENDING 지원자들을 WITHDRAWN 처리
        checkAndWithdrawPendingApplications(application);

        return convertToResponse(application);
    }

    private void addToTeam(Application application) {
        try {
            // 프로젝트에 연결된 팀 찾기
            Team team = findOrCreateTeamForProject(application.getProject());

            // 포지션 정보를 기반으로 Role 결정 (기본값은 MEMBER)
            com.metaverse.moem.team.domain.Role role = com.metaverse.moem.team.domain.Role.MEMBER;
            if (application.getAppliedPosition() != null && !application.getAppliedPosition().isEmpty()) {
                try {
                    // 포지션 이름을 Role enum으로 변환 시도
                    String positionUpper = application.getAppliedPosition().toUpperCase();
                    role = com.metaverse.moem.team.domain.Role.valueOf(positionUpper);
                } catch (IllegalArgumentException e) {
                    // 유효하지 않은 Role이면 기본값 사용
                    role = com.metaverse.moem.team.domain.Role.MEMBER;
                }
            }

            // TeamMembersService를 통해 팀 멤버 추가
            Long applicantId = application.getApplicantId();
            if (applicantId == null) {
                throw new RuntimeException("지원자 ID를 찾을 수 없습니다.");
            }
            com.metaverse.moem.team.dto.TeamMembersDto.CreateReq createReq = 
                new com.metaverse.moem.team.dto.TeamMembersDto.CreateReq(applicantId);
            
            teamMembersService.create(team.getId(), createReq, role);
        } catch (Exception e) {
            // 팀 멤버 추가 실패 시 지원 상태 롤백
            application.setStatus(Application.ApplicationStatus.PENDING);
            applicationRepository.save(application);
            throw new RuntimeException("팀 멤버 추가에 실패했습니다: " + e.getMessage());
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

    // 포지션이 모집완료되었는지 확인하고, 완료되었다면 나머지 PENDING 지원자들을 WITHDRAWN 처리
    private void checkAndWithdrawPendingApplications(Application approvedApplication) {
        String appliedPosition = approvedApplication.getAppliedPosition();
        if (appliedPosition == null || appliedPosition.trim().isEmpty()) {
            return; // 포지션이 없으면 처리하지 않음
        }

        ProjectPost project = approvedApplication.getProject();
        List<ProjectPost.Position> positions = project.getPositions();
        if (positions == null || positions.isEmpty()) {
            return; // 포지션이 없으면 처리하지 않음
        }

        // 해당 포지션 찾기
        ProjectPost.Position targetPosition = positions.stream()
                .filter(pos -> pos.getRole() != null && pos.getRole().equals(appliedPosition))
                .findFirst()
                .orElse(null);

        if (targetPosition == null) {
            return; // 해당 포지션이 없으면 처리하지 않음
        }

        Integer required = targetPosition.getHeadcount() != null ? targetPosition.getHeadcount() : 0;
        if (required <= 0) {
            return; // 필요 인원이 없으면 처리하지 않음
        }

        // 해당 포지션의 승인된 지원자 수 확인
        List<Application> approvedApplications = applicationRepository.findByProject_Id(project.getId())
                .stream()
                .filter(app -> app.getStatus() == Application.ApplicationStatus.APPROVED)
                .filter(app -> appliedPosition.equals(app.getAppliedPosition()))
                .collect(Collectors.toList());

        int current = approvedApplications.size();
        
        // 모집완료되었는지 확인
        if (current >= required) {
            // 해당 포지션의 PENDING 상태 지원자들을 WITHDRAWN 처리
            List<Application> pendingApplications = applicationRepository.findByProject_Id(project.getId())
                    .stream()
                    .filter(app -> app.getStatus() == Application.ApplicationStatus.PENDING)
                    .filter(app -> appliedPosition.equals(app.getAppliedPosition()))
                    .collect(Collectors.toList());

            for (Application pendingApp : pendingApplications) {
                pendingApp.setStatus(Application.ApplicationStatus.WITHDRAWN);
                applicationRepository.save(pendingApp);
            }
        }
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

