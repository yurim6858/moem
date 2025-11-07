package com.metaverse.moem.team.controller;

import com.metaverse.moem.auth.domain.User;
import com.metaverse.moem.auth.repository.UserRepository;
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
    private final UserRepository userRepository;


    @PostMapping("/projects/{projectId}")
    @ResponseStatus(HttpStatus.CREATED)
    public TeamDto.Res create(@PathVariable Long projectId,
                              @RequestBody @Valid TeamDto.CreateReq req) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("프로젝트를 찾을 수 없습니다."));
        return teamService.create(project, req);
    }

    @PutMapping("/{teamId}")
    public TeamDto.Res update(@PathVariable Long teamId,
                              @RequestBody @Valid TeamDto.UpdateReq req) {
        return teamService.update(teamId, req);
    }

    @DeleteMapping("/{teamId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long teamId) {
        teamService.delete(teamId);
    }

    @GetMapping
    public List<TeamDto.Res> list() {
        return teamService.list();
    }

    // 구체적인 경로를 먼저 선언 (Spring 경로 매칭 우선순위)
    @GetMapping("/my")
    public ResponseEntity<List<TeamDto.Res>> getMyTeams(@RequestHeader(value = "X-Username", required = false) String username) {
        if (username == null || username.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + username));
        
        List<TeamDto.Res> myTeams = teamService.getMyTeams(user.getId());
        return ResponseEntity.ok(myTeams);
    }

    // 프로젝트 시작 준비 상태 확인 (구체적인 경로를 먼저)
    @GetMapping("/{teamId}/start-ready/{projectId}")
    public ResponseEntity<TeamDto.StartReadyRes> checkStartReady(@PathVariable Long teamId, @PathVariable Long projectId) {
        TeamDto.StartReadyRes startReady = teamService.checkStartReady(teamId, projectId);
        return ResponseEntity.ok(startReady);
    }

    // 팀 상세 정보 조회 (RESTful 스타일) - 변수 경로는 가장 마지막에
    @GetMapping("/{teamId}")
    public ResponseEntity<TeamDto.DetailRes> getTeamDetail(@PathVariable Long teamId) {
        TeamDto.DetailRes teamInfo = teamService.getTeamInfo(teamId);
        return ResponseEntity.ok(teamInfo);
    }

    // 팀 상세 정보 조회 (하위 호환성을 위해 유지, deprecated)
    @GetMapping("/{teamId}/info")
    @Deprecated
    public ResponseEntity<TeamDto.DetailRes> getTeamInfo(@PathVariable Long teamId) {
        TeamDto.DetailRes teamInfo = teamService.getTeamInfo(teamId);
        return ResponseEntity.ok(teamInfo);
    }

    // 프로젝트 시작
    @PostMapping("/{teamId}/start-project")
    public ResponseEntity<com.metaverse.moem.project.dto.ProjectDto.Res> startProject(
            @PathVariable Long teamId,
            @RequestBody @Valid TeamDto.StartProjectReq req,
            @RequestHeader(value = "X-Username", required = false) String username) {
        
        if (username == null || username.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + username));
        
        com.metaverse.moem.project.dto.ProjectDto.Res project = teamService.startProject(teamId, req.projectId(), user.getId());
        return ResponseEntity.ok(project);
    }
}