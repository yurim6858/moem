package com.metaverse.moem.team.service;

import com.metaverse.moem.project.domain.Project;
import com.metaverse.moem.team.domain.Team;
import com.metaverse.moem.team.dto.TeamDto;
import com.metaverse.moem.team.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;

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
}
