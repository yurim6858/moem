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

    // 팀원 생성
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TeamMembersDto.Res create(@PathVariable Long teamId, @RequestBody @Valid TeamMembersDto.CreateReq req) {
        return  teamMembersService.create(teamId, req);
    }

    // 팀원 조회
    @GetMapping
    public List<TeamMembersDto.Res> getMembers(@PathVariable Long teamId) {
        return teamMembersService.getMembersByTeamId(teamId);
    }

    // 팀원 수정
    @PutMapping("/{memberId}")
    public TeamMembersDto.Res update(@PathVariable Long teamId,
                                     @PathVariable Long memberId,
                                     @RequestBody @Valid TeamMembersDto.UpdateReq req) {

        TeamMembersDto.UpdateReq updateReq = new TeamMembersDto.UpdateReq(memberId, req.name(), req.role());
        return teamMembersService.update(updateReq);
    }

    // 팀원 삭제
    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long teamId, @PathVariable Long memberId) {
        teamMembersService.delete(new TeamMembersDto.DeleteReq(memberId));
    }
}
