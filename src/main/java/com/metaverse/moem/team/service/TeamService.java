package com.metaverse.moem.team.service;

import com.metaverse.moem.team.domain.Team;
import com.metaverse.moem.team.dto.TeamDto;
import com.metaverse.moem.team.repository.TeamRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Transactional
public class TeamService {

    private final TeamRepository teamRepository;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public TeamService(TeamRepository teamRepository) {
        this.teamRepository = teamRepository;
    }


    public TeamDto.Res create(TeamDto.CreateReq req) {
        if (teamRepository.existsByName(req.name())) {
            throw new IllegalArgumentException("이미 존재하는 팀 이름입니다.");
        }
        Team saved = teamRepository.save(
                Team.builder().name(req.name()).description(req.description()).build());
        return toRes(saved);
    }


    public TeamDto.Res update(Long id, TeamDto.UpdateReq req) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("팀을 찾을 수 없습니다."));


        team.setName(req.name());
        team.setDescription(req.description());


        Team updated = teamRepository.save(team);
        return toRes(updated);
    }


    public void delete(Long id) {
        if (!teamRepository.existsById(id)) {
            throw new IllegalArgumentException("팀을 찾을 수 없습니다.");
        }
        teamRepository.deleteById(id);
    }


    @Transactional(readOnly = true)
    public List<TeamDto.Res> list() {
        return teamRepository.findAll().stream().map(this::toRes)      // → TeamDto.Res 로 매핑
                .toList();
    }


    private TeamDto.Res toRes(Team team) {
        return new TeamDto.Res(
                team.getId(),
                team.getName(),
                team.getDescription(),
                team.getCreatedAt() != null ? team.getCreatedAt().format(FMT) : null,
                team.getUpdatedAt() != null ? team.getUpdatedAt().format(FMT) : null);
    }
}
