package com.metaverse.moem.schedule.team_schedule.controller;

import com.metaverse.moem.schedule.team_schedule.dto.TeamScheduleDto;
import com.metaverse.moem.schedule.team_schedule.service.TeamScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/team-schedules")
public class TeamScheduleController {

    private final TeamScheduleService teamScheduleService;

    @GetMapping("/{teamId}")
    public List<TeamScheduleDto.Res> getTeamSchedules(@PathVariable Long teamId) {
        return teamScheduleService.getSchedules(teamId);
    }
}
