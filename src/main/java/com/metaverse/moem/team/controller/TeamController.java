package com.metaverse.moem.team.controller;

import com.metaverse.moem.project.domain.Project;
import com.metaverse.moem.project.repository.ProjectRepository;
import com.metaverse.moem.team.dto.TeamDto;
import com.metaverse.moem.team.service.TeamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;
    private final ProjectRepository projectRepository;


    @PostMapping("/projects/{projectId}/teams")
    @ResponseStatus(HttpStatus.CREATED)
    public TeamDto.Res create(@PathVariable Long projectId,
                              @RequestBody @Valid TeamDto.CreateReq req) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("프로젝트를 찾을 수 없습니다."));
        return teamService.create(project, req);
    }

    @PutMapping("/teams/{teamId}")
    public TeamDto.Res update(@PathVariable Long teamId,
                              @RequestBody @Valid TeamDto.UpdateReq req) {
        return teamService.update(teamId, req);
    }

    @DeleteMapping("/teams/{teamId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long teamId) {
        teamService.delete(teamId);
    }

    @GetMapping("/teams")
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