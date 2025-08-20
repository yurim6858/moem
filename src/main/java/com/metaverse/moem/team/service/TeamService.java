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

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public TeamService(TeamRepository teamRepository) {
        this.teamRepository = teamRepository;
    }

    // 팀 생성
    public TeamDto.Res create(TeamDto.CreateReq req) {
        if (teamRepository.existsByName(req.name())) {
            throw new IllegalArgumentException("이미 존재하는 팀 이름입니다.");
        }
        Team saved = teamRepository.save(
                Team.builder()
                        .name(req.name())
                        .description(req.description())
                        .build()
        );
        return toRes(saved);
    }

    // 팀 수정
    public TeamDto.Res update(Long id, TeamDto.UpdateReq req) {
        Team team = teamRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("팀을 찾을 수 없습니다."));

        // 값 변경
        team.setName(req.name());
        team.setDescription(req.description());

        // 저장 후 응답 반환
        Team updated = teamRepository.save(team);
        return toRes(updated);
    }

    // 팀 삭제
    public void delete(Long id) {
        if (!teamRepository.existsById(id)) {
            throw new IllegalArgumentException("팀을 찾을 수 없습니다.");
        }
        teamRepository.deleteById(id);
    }

    // 팀 목록 조회
    @Transactional(readOnly = true)
    public List<TeamDto.Res> list() {
        return teamRepository.findAll()
                .stream()
                .map(this::toRes)      // → TeamDto.Res 로 매핑
                .toList();
    }

    // Entity -> 응답 DTO 변환기
    private TeamDto.Res toRes(Team team) {   // ✅ 반환 타입 수정
        return new TeamDto.Res(
                team.getId(),
                team.getName(),
                team.getDescription(),
                team.getCreatedAt() != null ? team.getCreatedAt().format(FMT) : null,
                team.getUpdatedAt() != null ? team.getUpdatedAt().format(FMT) : null
        );
    }
}
