package com.metaverse.moem.team.controller;

import com.metaverse.moem.team.dto.TeamDto;
import com.metaverse.moem.team.service.TeamService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teams")
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

    // 내가 속한 팀 목록 조회
    @GetMapping("/my")
    public ResponseEntity<List<TeamDto.Res>> getMyTeams(@RequestHeader("X-Username") String username) {
        try {
            List<TeamDto.Res> teams = teamService.getMyTeams(username);
            return ResponseEntity.ok(teams);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // 팀 상세 정보 조회 (멤버 포함)
    @GetMapping("/{teamId}/info")
    public ResponseEntity<TeamDto.DetailRes> getTeamInfo(@PathVariable Long teamId) {
        try {
            TeamDto.DetailRes teamInfo = teamService.getTeamInfo(teamId);
            return ResponseEntity.ok(teamInfo);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // 프로젝트 시작 준비 상태 확인
    @GetMapping("/{teamId}/start-ready/{projectId}")
    public ResponseEntity<TeamDto.StartReadyRes> checkStartReady(@PathVariable Long teamId, @PathVariable Long projectId) {
        try {
            TeamDto.StartReadyRes startReady = teamService.checkStartReady(teamId, projectId);
            return ResponseEntity.ok(startReady);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // 프로젝트 시작
    @PostMapping("/{teamId}/start-project")
    public ResponseEntity<String> startProject(@PathVariable Long teamId, @RequestBody TeamDto.StartProjectReq req) {
        try {
            teamService.startProject(teamId, req.projectId());
            return ResponseEntity.ok("프로젝트가 성공적으로 시작되었습니다!");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("프로젝트 시작 중 오류가 발생했습니다.");
        }
    }
}