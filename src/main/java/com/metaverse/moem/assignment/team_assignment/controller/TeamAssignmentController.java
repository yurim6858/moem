package com.metaverse.moem.assignment.team_assignment.controller;

import com.metaverse.moem.assignment.team_assignment.dto.TeamAssignmentDto;
import com.metaverse.moem.assignment.team_assignment.service.TeamAssignmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/team-assignments")
@RequiredArgsConstructor
public class TeamAssignmentController {

    private final TeamAssignmentService teamAssignmentService;

    @PostMapping
    public TeamAssignmentDto.Res create(@RequestBody @Valid TeamAssignmentDto.CreateReq req) {
        return teamAssignmentService.create(req);
    }

    @GetMapping("/project/{projectId}")
    public List<TeamAssignmentDto.Res> getByProject(@PathVariable Long projectId) {
        return teamAssignmentService.getByProject(projectId);
    }

    @GetMapping("/project/{projectId}/user/{userId}")
    public List<TeamAssignmentDto.Res> getByProjectAndUser(@PathVariable Long projectId,
                                                           @PathVariable Long userId) {
        return teamAssignmentService.getByProjectAndUser(projectId, userId);
    }

    @PutMapping("/{teamAssignmentId}")
    public TeamAssignmentDto.Res update(@PathVariable Long teamAssignmentId,
                                        @RequestBody @Valid TeamAssignmentDto.UpdateReq req) {
        return teamAssignmentService.update(teamAssignmentId, req);
    }

    @DeleteMapping("/{teamAssignmentId}")
    public void delete(@PathVariable Long teamAssignmentId) {
        teamAssignmentService.delete(teamAssignmentId);
    }
}
