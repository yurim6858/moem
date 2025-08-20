package com.metaverse.moem.team.controller;

import com.metaverse.moem.team.dto.TeamMembersDto;
import com.metaverse.moem.team.service.TeamMembersService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/team/{teamId}/members")
@RequiredArgsConstructor
public class TeamMembersController {

    private final TeamMembersService teamMembersService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TeamMembersDto.Res create(@PathVariable Long teamId,
                                     @RequestBody @Valid TeamMembersDto.CreateReq req) {
        return teamMembersService.create(teamId, req);
    }

    @GetMapping
    public List<TeamMembersDto.Res> getMembers(@PathVariable Long teamId) {
        return teamMembersService.getMembersByTeamId(teamId);
    }

    @PutMapping("/{memberId}")
    public TeamMembersDto.Res update(@PathVariable Long memberId,
                                     @RequestBody @Valid TeamMembersDto.UpdateReq req) {
        return teamMembersService.update(memberId, req);
    }

    @DeleteMapping("/{memberId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long memberId) {
        teamMembersService.delete(new TeamMembersDto.DeleteReq(memberId));
    }
}
