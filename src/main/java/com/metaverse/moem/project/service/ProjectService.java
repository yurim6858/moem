package com.metaverse.moem.project.service;

import com.metaverse.moem.project.domain.Project;
import com.metaverse.moem.project.dto.ProjectDto;
import com.metaverse.moem.project.repository.ProjectRepository;
import com.metaverse.moem.auth.domain.User;
import com.metaverse.moem.auth.repository.UserRepository;
import com.metaverse.moem.team.domain.Team;
import com.metaverse.moem.team.domain.TeamMembers;
import com.metaverse.moem.team.repository.TeamMembersRepository;
import com.metaverse.moem.team.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final TeamRepository teamRepository;
    private final TeamMembersRepository teamMembersRepository;
    private final UserRepository userRepository;

    @Transactional
    public ProjectDto.Res create(ProjectDto.CreateReq req) {
        Project project = new Project();
        project.setName(req.name());
        project.setDescription(req.description());
        project.setType(req.type());
        project.setRecruitTotal(req.recruitTotal());
        project.setRecruitCurrent(0);
        project.setRecruitStartDate(req.recruitStartDate());
        project.setRecruitEndDate(req.recruitEndDate());
        project.setProjectStartDate(req.projectStartDate());
        project.setProjectEndDate(req.projectEndDate());

        projectRepository.save(project);

        // maxMembers는 recruitTotal + 팀장(1명)으로 설정
        Integer recruitTotal = req.recruitTotal() != null ? req.recruitTotal() : 0;
        Integer maxMembers = recruitTotal + 1; // 팀장 포함
        Team team = Team.create(project, req.teamName(), maxMembers);
        teamRepository.save(team);

        project.setTeam(team);

        return ProjectDto.Res.from(project);
    }

    @Transactional(readOnly = true)
    public Optional<ProjectDto.Res> get(Long id) {
        return projectRepository.findByIdAndIsDeletedFalse(id).map(ProjectDto.Res::from);
    }

    @Transactional(readOnly = true)
    public List<ProjectDto.Res> listPublic(ProjectDto.SearchCondition condition) {
        return projectRepository.searchPublic(condition.type(), condition.status(), condition.query())
                .stream().map(ProjectDto.Res::from).toList();
    }

    @Transactional
    public Optional<ProjectDto.Res> update(Long id, ProjectDto.UpdateReq req) {
        return projectRepository.findByIdAndIsDeletedFalse(id)
                .map(project -> {
                    project.setName(req.name());
                    project.setDescription(req.description());
                    project.setType(req.type());
                    project.setRecruitTotal(req.recruitTotal());
                    project.setRecruitStartDate(req.recruitStartDate());
                    project.setRecruitEndDate(req.recruitEndDate());
                    project.setProjectStartDate(req.projectStartDate());
                    project.setProjectEndDate(req.projectEndDate());
                    return ProjectDto.Res.from(project);
                });
    }

    @Transactional
    public boolean delete(Long id) {
        return projectRepository.findByIdAndIsDeletedFalse(id)
                .map(project -> {
                    project.setDeleted(true);
                    return true;
                })
                .orElse(false);
    }

    // 내가 속한 프로젝트 목록 조회 (프로젝트 시작 후)
    @Transactional(readOnly = true)
    public List<ProjectDto.Res> getMyProjects(Long userId) {
        // TeamMembers를 통해 사용자가 속한 팀 찾기
        List<TeamMembers> myMemberships = teamMembersRepository.findByUserId(userId);
        
        return myMemberships.stream()
                .map(TeamMembers::getTeam)
                .filter(team -> team != null)
                .map(team -> {
                    try {
                        return team.getProject();
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(project -> project != null)
                .filter(project -> !project.isDeleted())
                // 프로젝트 시작 후만 포함 (projectStartDate가 null이 아닌 경우)
                .filter(project -> project.getProjectStartDate() != null)
                .map(ProjectDto.Res::from)
                .distinct()
                .collect(Collectors.toList());
    }

    // 시작된 프로젝트 상세 조회 (사용자 인증 확인)
    @Transactional(readOnly = true)
    public Optional<ProjectDto.Res> getStartedProject(Long projectId, String username) {
        User user = userRepository.findByUsername(username)
                .orElse(null);
        
        if (user == null) {
            return Optional.empty();
        }

        // 프로젝트 조회
        Optional<Project> projectOpt = projectRepository.findById(projectId);
        if (projectOpt.isEmpty() || projectOpt.get().isDeleted()) {
            return Optional.empty();
        }

        Project project = projectOpt.get();
        
        // 프로젝트가 시작되었는지 확인 (projectStartDate가 null이 아닌 경우)
        if (project.getProjectStartDate() == null) {
            return Optional.empty();
        }

        // 사용자가 해당 프로젝트의 팀 멤버인지 확인
        if (project.getTeam() == null) {
            return Optional.empty();
        }

        List<TeamMembers> members = teamMembersRepository.findByTeamId(project.getTeam().getId());
        boolean isMember = members.stream()
                .anyMatch(member -> member.getUserId().equals(user.getId()));

        if (!isMember) {
            return Optional.empty();
        }

        return Optional.of(ProjectDto.Res.from(project));
    }

    // 프로젝트 종료
    @Transactional
    public ProjectDto.Res endProject(Long projectId, Long userId) {
        // 프로젝트 조회
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("프로젝트를 찾을 수 없습니다. id = " + projectId));

        // 프로젝트가 삭제되었는지 확인
        if (project.isDeleted()) {
            throw new IllegalArgumentException("삭제된 프로젝트입니다.");
        }

        // 프로젝트가 시작되었는지 확인
        if (project.getProjectStartDate() == null) {
            throw new IllegalArgumentException("시작되지 않은 프로젝트입니다.");
        }

        // 이미 종료된 프로젝트인지 확인
        if (project.getProjectEndDate() != null) {
            throw new IllegalArgumentException("이미 종료된 프로젝트입니다.");
        }

        // 사용자가 팀 리더인지 확인
        if (project.getTeam() == null) {
            throw new IllegalArgumentException("팀 정보를 찾을 수 없습니다.");
        }

        List<TeamMembers> members = teamMembersRepository.findByTeamId(project.getTeam().getId());
        boolean isLeader = members.stream()
                .anyMatch(member -> member.getUserId().equals(userId) 
                        && member.getRole() == com.metaverse.moem.team.domain.Role.MANAGER);

        if (!isLeader) {
            throw new IllegalArgumentException("프로젝트를 종료할 권한이 없습니다. 팀 리더만 프로젝트를 종료할 수 있습니다.");
        }

        // 프로젝트 종료 날짜 설정
        project.setProjectEndDate(java.time.LocalDate.now());
        projectRepository.save(project);

        return ProjectDto.Res.from(project);
    }
}
