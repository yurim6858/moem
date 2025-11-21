package com.metaverse.moem.schedule.personal_schedule.controller;

import com.metaverse.moem.schedule.personal_schedule.dto.PersonalScheduleDto;
import com.metaverse.moem.schedule.personal_schedule.service.PersonalScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/personal-schedules")
@RequiredArgsConstructor
public class PersonalScheduleController {

    private final PersonalScheduleService personalScheduleService;

    @GetMapping("/user/{userId}")
    public List<PersonalScheduleDto.Res> getSchedules(@PathVariable Long userId) {
        return personalScheduleService.getSchedules(userId);
    }

}
