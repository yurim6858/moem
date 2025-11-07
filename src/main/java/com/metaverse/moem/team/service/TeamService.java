package com.metaverse.moem.team.service;

import com.metaverse.moem.application.domain.Application;
import com.metaverse.moem.application.repository.ApplicationRepository;
import com.metaverse.moem.auth.repository.UserRepository;
import com.metaverse.moem.matching.domain.ProjectPost;
import com.metaverse.moem.matching.repository.ProjectPostRepository;
import com.metaverse.moem.project.domain.Project;
import com.metaverse.moem.project.dto.ProjectDto;
import com.metaverse.moem.project.repository.ProjectRepository;
import com.metaverse.moem.team.domain.Team;
import com.metaverse.moem.team.domain.TeamMembers;
import com.metaverse.moem.team.dto.TeamDto;
import com.metaverse.moem.team.dto.TeamMembersDto;
import com.metaverse.moem.team.repository.TeamMembersRepository;
import com.metaverse.moem.team.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;
    private final TeamMembersRepository teamMembersRepository;
    private final ProjectPostRepository projectPostRepository;
    private final UserRepository userRepository;
    private final ApplicationRepository applicationRepository;
    private final ProjectRepository projectRepository;

    public TeamDto.Res create(Project project, TeamDto.CreateReq req) {
        if (req.name() != null && teamRepository.existsByName(req.name())) {
            throw new IllegalArgumentException("이미 존재하는 팀 이름입니다.");
        }
        Team team = Team.create(project, req.name(), req.maxMembers());
        teamRepository.save(team);
        return TeamDto.Res.from(team);
    }

    public TeamDto.Res update(Long id, TeamDto.UpdateReq req) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("팀을 찾을 수 없습니다. id = " + id));

        team.updateInfo(req.name(), req.maxMembers());
        return TeamDto.Res.from(team);
    }

    public void delete(Long id) {
        if (!teamRepository.existsById(id)) {
            throw new IllegalArgumentException("팀을 찾을 수 없습니다. id = " + id);
        }
        teamRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<TeamDto.Res> list() {
        return teamRepository.findAll()
                .stream()
                .map(TeamDto.Res::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TeamDto.Res> getMyTeams(Long userId) {
        List<TeamMembers> myMemberships = teamMembersRepository.findByUserId(userId);
        return myMemberships.stream()
                .map(TeamMembers::getTeam)
                .filter(team -> team != null)  // team이 null인 경우 필터링
                // 삭제된 ProjectPost와 연결된 Team은 제외
                .filter(team -> {
                    // Team과 연결된 ProjectPost를 조회
                    Optional<ProjectPost> projectPostOpt = projectPostRepository.findByTeam_Id(team.getId());
                    if (projectPostOpt.isEmpty()) {
                        // ProjectPost가 없는 Team은 제외
                        return false;
                    }
                    ProjectPost projectPost = projectPostOpt.get();
                    // 삭제되지 않은 ProjectPost와 연결된 Team만 포함
                    return !projectPost.isDeleted();
                })
                // 프로젝트 시작 전 팀만 포함 (projectStartDate가 null인 경우)
                .filter(team -> {
                    try {
                        Project project = team.getProject();
                        // Project가 없거나 projectStartDate가 null이면 시작 전 팀
                        return project == null || project.getProjectStartDate() == null;
                    } catch (Exception e) {
                        // LAZY 로딩 실패 시 false 반환 (안전하게 제외)
                        return false;
                    }
                })
                .map(TeamDto.Res::from)
                .distinct()  // 중복 제거 (혹시 모를 경우 대비)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TeamDto.DetailRes getTeamInfo(Long teamId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("팀을 찾을 수 없습니다. id = " + teamId));

        // 프로젝트가 시작되지 않은 경우에만 삭제된 ProjectPost 체크
        // 프로젝트가 시작된 후에는 ProjectPost가 삭제되지만, 팀 정보는 조회 가능해야 함
        try {
            Project project = team.getProject();
            if (project != null && project.getProjectStartDate() == null) {
                // 프로젝트 시작 전: 삭제된 ProjectPost와 연결된 Team은 조회 불가
                // 단, 공고가 모집완료 상태인 경우는 조회 가능 (프로젝트 시작 준비 중)
                Optional<ProjectPost> projectPostOpt = projectPostRepository.findByTeam_Id(teamId);
                if (projectPostOpt.isPresent()) {
                    ProjectPost projectPost = projectPostOpt.get();
                    // 삭제된 공고와 연결된 팀은 조회 불가
                    if (projectPost.isDeleted()) {
                        throw new IllegalArgumentException("삭제된 프로젝트의 팀 정보입니다.");
                    }
                }
            }
            // 프로젝트 시작 후: ProjectPost 삭제 여부와 관계없이 팀 정보 조회 가능
        } catch (IllegalArgumentException e) {
            // 명시적인 IllegalArgumentException은 다시 던지기
            throw e;
        } catch (Exception e) {
            // LAZY 로딩 실패 등 기타 예외는 무시하고 계속 진행
            // 프로젝트 정보를 가져올 수 없어도 팀 정보는 조회 가능해야 함
        }

        // members 리스트를 안전하게 생성 (null 체크 및 필터링)
        // LAZY 로딩을 위해 트랜잭션 내에서 명시적으로 접근
        List<TeamMembersDto.Res> members = teamMembersRepository.findByTeamId(teamId)
                .stream()
                .filter(member -> member != null)  // null 항목 필터링
                .filter(member -> {
                    // team이 null이 아닌지 확인 (LAZY 로딩 안전성)
                    try {
                        return member.getTeam() != null;
                    } catch (Exception e) {
                        return false;
                    }
                })
                .map(member -> {
                    try {
                        // UserRepository를 사용하여 username, email 정보 포함
                        return TeamMembersDto.Res.from(member, userRepository);
                    } catch (Exception e) {
                        // 변환 실패 시 기본 버전 사용
                        try {
                            return TeamMembersDto.Res.from(member);
                        } catch (Exception e2) {
                            return null;
                        }
                    }
                })
                .filter(dto -> dto != null)  // DTO 변환 실패 항목 필터링
                .collect(Collectors.toList());

        // 빈 리스트 보장
        if (members == null) {
            members = java.util.Collections.emptyList();
        }

        // Project 정보 가져오기 (LAZY 로딩 안전성)
        Long projectId = null;
        try {
            Project project = team.getProject();
            if (project != null) {
                projectId = project.getId();
            }
        } catch (Exception e) {
            // LAZY 로딩 실패 시 null 유지
            projectId = null;
        }

        // null 안전성 보장 - 모든 필드에 기본값 제공
        return new TeamDto.DetailRes(
                team.getId() != null ? team.getId() : 0L,
                team.getName() != null ? team.getName() : "",
                team.getMaxMembers() != null ? team.getMaxMembers() : 0,
                team.getCreatedAt() != null ? team.getCreatedAt().toString() : "",
                team.getUpdatedAt() != null ? team.getUpdatedAt().toString() : "",
                members,
                members.size(),
                projectId
        );
    }

    @Transactional(readOnly = true)
    public TeamDto.StartReadyRes checkStartReady(Long teamId, Long projectId) {
        // 팀 존재 확인
        if (!teamRepository.existsById(teamId)) {
            throw new IllegalArgumentException("팀을 찾을 수 없습니다. id = " + teamId);
        }

        // 프로젝트와 연결된 ProjectPost 조회
        Optional<ProjectPost> projectPostOpt = projectPostRepository.findByTeam_Id(teamId);
        if (projectPostOpt.isEmpty() || projectPostOpt.get().isDeleted()) {
            throw new IllegalArgumentException("프로젝트를 찾을 수 없습니다.");
        }
        // 모집완료된 공고는 프로젝트 시작 준비 확인 가능 (프로젝트 시작 전 모집완료 상태)
        // 프로젝트가 이미 시작되었는지는 projectStartDate로 확인
        ProjectPost projectPost = projectPostOpt.get();

        // ProjectPost의 positions 정보 가져오기
        List<ProjectPost.Position> positions = projectPost.getPositions();
        if (positions == null || positions.isEmpty()) {
            // positions가 없으면 기본값 반환
            return new TeamDto.StartReadyRes(
                    false,
                    0,
                    0,
                    0.0,
                    java.util.Collections.emptyList(),
                    "프로젝트 포지션 정보가 없습니다."
            );
        }

        // 실제 팀 멤버 ID 목록 가져오기
        List<Long> teamMemberIds = teamMembersRepository.findByTeamId(teamId)
                .stream()
                .map(TeamMembers::getUserId)
                .collect(Collectors.toList());

        // 승인된 Application들을 포지션별로 그룹화하여 카운트 (실제 팀 멤버만 카운트)
        List<Application> approvedApplications = applicationRepository.findByProject_Id(projectPost.getId())
                .stream()
                .filter(app -> app.getStatus() == Application.ApplicationStatus.APPROVED)
                .filter(app -> {
                    // 실제 팀 멤버인지 확인
                    try {
                        Long applicantId = app.getApplicantId();
                        return applicantId != null && teamMemberIds.contains(applicantId);
                    } catch (Exception e) {
                        return false;
                    }
                })
                .collect(Collectors.toList());

        // 포지션별로 승인된 Application 수를 카운트
        Map<String, Long> positionCountMap = approvedApplications.stream()
                .filter(app -> app.getAppliedPosition() != null && !app.getAppliedPosition().trim().isEmpty())
                .collect(Collectors.groupingBy(
                        Application::getAppliedPosition,
                        Collectors.counting()
                ));

        // 각 position별 상태 계산
        List<TeamDto.PositionStatus> positionStatuses = new java.util.ArrayList<>();
        int totalRequired = 0;
        int totalFilled = 0;

        for (ProjectPost.Position position : positions) {
            String role = position.getRole();
            Integer required = position.getHeadcount() != null ? position.getHeadcount() : 0;
            totalRequired += required;

            // 해당 포지션에 승인된 Application 수 확인
            Long approvedCount = positionCountMap.getOrDefault(role, 0L);
            int current = approvedCount.intValue();

            totalFilled += current;
            boolean isFilled = current >= required;

            positionStatuses.add(new TeamDto.PositionStatus(
                    role != null ? role : "미지정",
                    required,
                    current,
                    isFilled
            ));
        }

        // 전체 완성률 계산
        double completionRate = totalRequired > 0 
                ? (double) Math.min(totalFilled, totalRequired) / totalRequired * 100.0 
                : 0.0;

        // 모든 포지션이 채워졌는지 확인
        boolean isReadyToStart = positionStatuses.stream()
                .allMatch(TeamDto.PositionStatus::isFilled);

        String message = isReadyToStart
                ? "모든 필수 포지션이 채워졌습니다. 프로젝트를 시작할 수 있습니다."
                : String.format("포지션 모집이 완료되지 않았습니다. (%d/%d 명)", totalFilled, totalRequired);

        return new TeamDto.StartReadyRes(
                isReadyToStart,
                totalRequired,
                totalFilled,
                completionRate,
                positionStatuses,
                message
        );
    }

    @Transactional
    public ProjectDto.Res startProject(Long teamId, Long projectId, Long userId) {
        // 팀 존재 확인
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("팀을 찾을 수 없습니다. id = " + teamId));

        // 프로젝트 존재 확인
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("프로젝트를 찾을 수 없습니다. id = " + projectId));

        // 팀과 프로젝트가 연결되어 있는지 확인
        if (!team.getProject().getId().equals(projectId)) {
            throw new IllegalArgumentException("팀과 프로젝트가 연결되어 있지 않습니다.");
        }

        // 이미 시작된 프로젝트인지 확인
        if (project.getProjectStartDate() != null) {
            throw new IllegalArgumentException("이미 시작된 프로젝트입니다.");
        }

        // 프로젝트 시작 준비 상태 확인
        TeamDto.StartReadyRes startReady = checkStartReady(teamId, projectId);
        if (!startReady.isReadyToStart()) {
            throw new IllegalArgumentException("프로젝트 시작 준비가 완료되지 않았습니다: " + startReady.message());
        }

        // 사용자가 팀 리더인지 확인
        List<TeamMembers> members = teamMembersRepository.findByTeamId(teamId);
        boolean isLeader = members.stream()
                .anyMatch(member -> member.getUserId().equals(userId) 
                        && member.getRole() == com.metaverse.moem.team.domain.Role.MANAGER);
        
        if (!isLeader) {
            throw new IllegalArgumentException("프로젝트를 시작할 권한이 없습니다. 팀 리더만 프로젝트를 시작할 수 있습니다.");
        }

        // 프로젝트 시작 날짜 설정
        project.setProjectStartDate(LocalDate.now());
        projectRepository.save(project);

        // 프로젝트 시작 시 공고(ProjectPost)를 모집완료 상태로 변경
        Optional<ProjectPost> projectPostOpt = projectPostRepository.findByTeam_Id(teamId);
        if (projectPostOpt.isPresent()) {
            ProjectPost projectPost = projectPostOpt.get();
            projectPost.setRecruitmentCompleted(true);
            projectPostRepository.save(projectPost);
        }

        return ProjectDto.Res.from(project);
    }
}
