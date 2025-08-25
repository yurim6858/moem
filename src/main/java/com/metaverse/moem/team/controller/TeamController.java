package com.metaverse.moem.team.controller;

import com.metaverse.moem.project.domain.Project;
import com.metaverse.moem.project.repository.ProjectRepository;
import com.metaverse.moem.team.dto.TeamDto;
import com.metaverse.moem.team.service.TeamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
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
}