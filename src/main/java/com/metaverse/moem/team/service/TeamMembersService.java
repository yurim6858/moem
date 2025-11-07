package com.metaverse.moem.team.service;

import com.metaverse.moem.application.domain.Application;
import com.metaverse.moem.application.repository.ApplicationRepository;
import com.metaverse.moem.auth.repository.UserRepository;
import com.metaverse.moem.matching.domain.ProjectPost;
import com.metaverse.moem.matching.repository.ProjectPostRepository;
import com.metaverse.moem.project.domain.Project;
import com.metaverse.moem.project.repository.ProjectRepository;
import com.metaverse.moem.team.domain.Role;
import com.metaverse.moem.team.domain.Team;
import com.metaverse.moem.team.domain.TeamMemberLeaveRequest;
import com.metaverse.moem.team.domain.TeamMembers;
import com.metaverse.moem.team.dto.TeamMembersDto;
import com.metaverse.moem.team.repository.TeamMemberLeaveRequestRepository;
import com.metaverse.moem.team.repository.TeamMembersRepository;
import com.metaverse.moem.team.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TeamMembersService {

    private final TeamMembersRepository teamMembersRepository;
    private final TeamRepository teamRepository;
    private final ProjectRepository projectRepository;
    private final TeamMemberLeaveRequestRepository leaveRequestRepository;
    private final UserRepository userRepository;
    private final ProjectPostRepository projectPostRepository;
    private final ApplicationRepository applicationRepository;

    public TeamMembersDto.Res create(Long teamId, TeamMembersDto.CreateReq req, Role role) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("팀을 찾을 수 없습니다. teamId = " + teamId));

        Long userId = req.userId();

        if (teamMembersRepository.existsByTeamIdAndUserId(teamId, userId)) {
            throw new IllegalArgumentException("이미 팀에 포함된 사용자입니다. userId = " + userId);
        }

        // 팀 정원 확인 및 자동 수정 (기존 팀의 잘못된 정원 설정 보정)
        Long current = teamMembersRepository.countByTeamId(teamId);
        
        // 팀 정원이 1로 잘못 설정된 경우, ProjectPost의 포지션 기반으로 자동 업데이트
        // 또는 정원이 현재 멤버 수보다 작거나 같으면 자동 수정
        if (team.getMaxMembers() != null && (team.getMaxMembers() <= current || team.getMaxMembers() == 1)) {
            // ProjectPost를 통해 올바른 정원 계산
            Optional<ProjectPost> projectPostOpt = projectPostRepository.findByTeam_Id(teamId);
            if (projectPostOpt.isPresent()) {
                ProjectPost projectPost = projectPostOpt.get();
                if (!projectPost.isDeleted() && projectPost.getPositions() != null) {
                    int totalHeadcount = projectPost.getPositions().stream()
                            .mapToInt(pos -> pos.getHeadcount() != null ? pos.getHeadcount() : 0)
                            .sum();
                    int correctMaxMembers = totalHeadcount + 1; // 팀장 포함
                    // 정원이 잘못 설정되었거나 너무 작으면 수정
                    if (correctMaxMembers > team.getMaxMembers() || team.getMaxMembers() == 1) {
                        team.updateInfo(null, correctMaxMembers);
                        teamRepository.save(team);
                        // 팀 객체 새로고침 (저장 후 최신 정보 반영)
                        team = teamRepository.findById(teamId).orElse(team);
                        System.out.println("팀 정원 자동 수정: " + (team.getMaxMembers() != null ? team.getMaxMembers() : "null") + " -> " + correctMaxMembers + " (현재 멤버: " + current + "명)");
                    }
                }
            }
        }
        
        // 정원 체크 (자동 수정 후 최신 정보로 확인)
        if (team.getMaxMembers() != null && team.getMaxMembers() > 0) {
            if (current >= team.getMaxMembers()) {
                throw new IllegalArgumentException("팀 정원이 초과되었습니다. (현재: " + current + "명, 최대: " + team.getMaxMembers() + "명)");
            }
        }

        TeamMembers members = TeamMembers.create(team, userId, role);
        TeamMembers saved = teamMembersRepository.save(members);

        Project project = team.getProject();
        if (project != null) {
            Integer cur = project.getRecruitCurrent() == null ? 0 : project.getRecruitCurrent();
            project.setRecruitCurrent(cur + 1);
            projectRepository.save(project);
        }

        // UserRepository를 사용하여 username, email 정보 포함
        return TeamMembersDto.Res.from(saved, userRepository);
    }

    public TeamMembersDto.Res update(Long memberId, TeamMembersDto.UpdateReq req) {
        TeamMembers member = teamMembersRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("해당 팀원을 찾을 수 없습니다. memberId = " + memberId));
        member.changeRole(req.role());
        // UserRepository를 사용하여 username, email 정보 포함
        return TeamMembersDto.Res.from(member, userRepository);
    }

    public void delete(Long memberId) {
        TeamMembers member = teamMembersRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("팀원을 찾을 수 없습니다. memberId = " + memberId));

        Team team = member.getTeam();
        Long userId = member.getUserId();
        
        if (team != null) {
            team.removeMember(member);
        }

        // TeamMembers 삭제 전에 관련된 TeamMemberLeaveRequest도 함께 삭제
        try {
            List<TeamMemberLeaveRequest> relatedRequests = leaveRequestRepository.findAllByTeamMember_Id(member.getId());
            if (relatedRequests != null && !relatedRequests.isEmpty()) {
                leaveRequestRepository.deleteAll(relatedRequests);
            }
        } catch (Exception e) {
            // TeamMemberLeaveRequest 삭제 실패 시에도 계속 진행 (로깅만)
            System.err.println("TeamMemberLeaveRequest 삭제 중 오류 발생: " + e.getMessage());
        }

        // 팀 멤버 삭제 전에 해당 사용자의 APPROVED 상태 Application을 WITHDRAWN으로 변경
        if (team != null && team.getProject() != null) {
            Optional<ProjectPost> projectPostOpt = projectPostRepository.findByTeam_Id(team.getId());
            
            if (projectPostOpt.isPresent() && !projectPostOpt.get().isDeleted()) {
                ProjectPost projectPost = projectPostOpt.get();
                // 해당 사용자의 APPROVED 상태 Application 찾기
                List<Application> userApplications = applicationRepository.findByProject_Id(projectPost.getId())
                        .stream()
                        .filter(app -> {
                            try {
                                Long applicantId = app.getApplicantId();
                                return applicantId != null && applicantId.equals(userId);
                            } catch (Exception e) {
                                return false;
                            }
                        })
                        .filter(app -> app.getStatus() == Application.ApplicationStatus.APPROVED)
                        .collect(Collectors.toList());
                
                // APPROVED 상태 Application을 WITHDRAWN으로 변경
                for (Application app : userApplications) {
                    app.setStatus(Application.ApplicationStatus.WITHDRAWN);
                    applicationRepository.save(app);
                    System.out.println("팀원 삭제로 인해 Application 상태 변경: APPROVED -> WITHDRAWN (Application ID: " + app.getId() + ", User ID: " + userId + ")");
                }
            }
        }

        teamMembersRepository.delete(member);

        if (team != null && team.getProject() != null) {
            Project project = team.getProject();
            int cur = project.getRecruitCurrent() == null ? 0 : project.getRecruitCurrent();
            project.setRecruitCurrent(Math.max(0, cur - 1));
            projectRepository.save(project);
            
            // 프로젝트가 시작 전이고 공고가 모집완료 상태인 경우, 인원 부족 시 다시 모집중으로 변경
            if (project.getProjectStartDate() == null) {
                checkAndReopenRecruitment(team, userId);
            }
        }
    }

    // 팀원이 나가서 인원이 부족해지면 공고를 다시 모집중으로 변경
    private void checkAndReopenRecruitment(Team team, Long leftUserId) {
        try {
            // 팀에 연결된 공고 찾기
            Optional<ProjectPost> projectPostOpt = projectPostRepository.findByTeam_Id(team.getId());
            if (projectPostOpt.isEmpty() || projectPostOpt.get().isDeleted()) {
                return; // 공고가 없거나 삭제된 경우 처리하지 않음
            }

            ProjectPost projectPost = projectPostOpt.get();
            
            // 프로젝트가 이미 시작되었으면 처리하지 않음
            if (team.getProject() != null && team.getProject().getProjectStartDate() != null) {
                return;
            }

            // 포지션별 인원 재계산
            List<ProjectPost.Position> positions = projectPost.getPositions();
            if (positions == null || positions.isEmpty()) {
                return;
            }

            // 모든 지원서 조회
            List<Application> allApplications = applicationRepository.findByProject_Id(projectPost.getId());
            
            // 포지션별 승인된 지원자 수 카운트 (나간 사용자 제외)
            Map<String, Long> approvedCountMap = allApplications.stream()
                    .filter(app -> app.getStatus() == Application.ApplicationStatus.APPROVED)
                    .filter(app -> {
                        // 나간 사용자는 제외
                        try {
                            Long applicantId = app.getApplicantId();
                            return applicantId != null && !applicantId.equals(leftUserId);
                        } catch (Exception e) {
                            return false;
                        }
                    })
                    .filter(app -> app.getAppliedPosition() != null && !app.getAppliedPosition().trim().isEmpty())
                    .collect(Collectors.groupingBy(
                            Application::getAppliedPosition,
                            Collectors.counting()
                    ));

            // 모든 포지션이 모집완료되었는지 확인
            boolean allPositionsCompleted = true;
            for (ProjectPost.Position position : positions) {
                String role = position.getRole();
                Integer required = position.getHeadcount() != null ? position.getHeadcount() : 0;
                
                if (required > 0) {
                    // 해당 포지션의 현재 승인된 인원 수 확인 (나간 사용자 제외)
                    Long approvedCount = approvedCountMap.getOrDefault(role, 0L);
                    int current = approvedCount.intValue();
                    
                    if (current < required) {
                        allPositionsCompleted = false;
                        break;
                    }
                }
            }

            // 모든 포지션이 모집완료되지 않았으면 공고를 모집중으로 변경
            if (!allPositionsCompleted) {
                projectPost.setRecruitmentCompleted(false);
                projectPostRepository.save(projectPost);
                System.out.println("팀원 삭제로 인해 공고 모집 상태가 모집중으로 변경되었습니다. (팀 ID: " + team.getId() + ")");
            } else {
                // 모든 포지션이 여전히 모집완료 상태면 모집완료 유지
                projectPost.setRecruitmentCompleted(true);
                projectPostRepository.save(projectPost);
                System.out.println("팀원 삭제 후에도 모든 포지션이 모집완료 상태입니다. (팀 ID: " + team.getId() + ")");
            }
        } catch (Exception e) {
            // 예외 발생 시 로깅만 하고 계속 진행 (팀원 제거는 성공으로 처리)
            System.err.println("공고 모집 상태 재평가 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Transactional(readOnly = true)
    public List<TeamMembersDto.Res> list(Long teamId) {
        if (!teamRepository.existsById(teamId)) {
            throw new IllegalArgumentException("해당 팀을 찾을 수 없습니다. teamId = " + teamId);
        }
        return teamMembersRepository.findByTeamId(teamId)
                .stream()
                .map(member -> TeamMembersDto.Res.from(member, userRepository))
                .toList();
    }
}
