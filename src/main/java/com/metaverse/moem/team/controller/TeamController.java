package com.metaverse.moem.team.controller;

import com.metaverse.moem.team.dto.TeamDto;
import com.metaverse.moem.team.service.TeamService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/team")
public class TeamController {

    private final TeamService teamService;

    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }


    // 팀 생성
    // 현재 JPA가 PK를 자동생성(@GeneratedValue) 하므로 teamId 경로값은 사용하지 않음
    // 라우팅 일치 목적

    @PostMapping("/{teamId}")
    @ResponseStatus(HttpStatus.CREATED)
    public TeamDto.Res create(@PathVariable Long teamId, @RequestBody @Valid TeamDto.CreateReq req) {
        return teamService.create(req);
    }

    // 팀 수정
    @PutMapping("/{teamId}")
    public TeamDto.Res update(@PathVariable Long teamId, @RequestBody @Valid TeamDto.UpdateReq req) {
        return teamService.update(teamId, req);
    }

    // 팀 삭제
    @DeleteMapping("/{teamId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long teamId) {
        teamService.delete(teamId);
    }

    @GetMapping
    public List<TeamDto.Res> list() {
        return teamService.list();
    }
}