package com.metaverse.moem.team.service;

import com.metaverse.moem.project.domain.Project;
import com.metaverse.moem.project.repository.ProjectRepository;
import com.metaverse.moem.team.domain.Role;
import com.metaverse.moem.team.domain.Team;
import com.metaverse.moem.team.domain.TeamMembers;
import com.metaverse.moem.team.dto.TeamMembersDto;
import com.metaverse.moem.team.repository.TeamMembersRepository;
import com.metaverse.moem.team.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TeamMembersService {

    private final TeamMembersRepository teamMembersRepository;
    private final TeamRepository teamRepository;
    private final ProjectRepository projectRepository;

    public TeamMembersDto.Res create(Long teamId, TeamMembersDto.CreateReq req, Role role) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new IllegalArgumentException("팀을 찾을 수 없습니다. teamId = " + teamId));

        Long userId = req.userId();

        if (teamMembersRepository.existsByTeamIdAndUserId(teamId, userId)) {
            throw new IllegalArgumentException("이미 팀에 포함된 사용자입니다. userId = " + userId);
        }

        if (team.getMaxMembers() != null && team.getMaxMembers() > 0) {
            Long current = teamMembersRepository.countByTeamId(teamId);
            if (current > team.getMaxMembers()) {
                throw new IllegalArgumentException("팀 정원이 초과되었습니다.");
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


        return TeamMembersDto.Res.from(saved);
    }


    public TeamMembersDto.Res update(Long memberId, TeamMembersDto.UpdateReq req) {
        TeamMembers member = teamMembersRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("해당 팀원을 찾을 수 없습니다. memberId = " + memberId));
        member.changeRole(req.role());
        return TeamMembersDto.Res.from(member);
    }


    public void delete(Long memberId) {

        TeamMembers member = teamMembersRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("팀원을 찾을 수 없습니다. memberId = " + memberId));


        Team team = member.getTeam();
        if (team != null) {
            team.removeMember(member);
        }

        teamMembersRepository.delete(member);

        if (team != null && team.getProject() != null) {
            Project project = team.getProject();
            int cur = project.getRecruitCurrent() == null ? 0 : project.getRecruitCurrent();
            project.setRecruitCurrent(Math.max(0, cur - 1));
            projectRepository.save(project);
        }
    }


    @Transactional(readOnly = true)
    public List<TeamMembersDto.Res> list(Long teamId) {
        if (!teamRepository.existsById(teamId)) {
            throw new IllegalArgumentException("해당 팀을 찾을 수 없습니다. teamId = " + teamId);
        }
        return teamMembersRepository.findByTeamId(teamId)
                .stream()
                .map(TeamMembersDto.Res::from)
                .toList();
    }
}
