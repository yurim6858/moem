package com.metaverse.moem.assignment.personal_assignment.controller;

import com.metaverse.moem.assignment.personal_assignment.dto.PersonalAssignmentDto;
import com.metaverse.moem.assignment.personal_assignment.service.PersonalAssignmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/personal-assignments")
@RequiredArgsConstructor
public class PersonalAssignmentController {

    private final PersonalAssignmentService personalAssignmentService;

    // 생성
    @PostMapping("/from-team")
    public PersonalAssignmentDto.Res create(@RequestBody @Valid PersonalAssignmentDto.CreateFromTeamReq req) {
        return personalAssignmentService.createFromTeam(req);
    }

    @PostMapping("/own")
    public PersonalAssignmentDto.Res create(@RequestBody @Valid PersonalAssignmentDto.CreateOwnReq req) {
        return personalAssignmentService.createOwn(req);
    }

    // 사용자 기반 전체 조회
    @GetMapping("/user/{userId}")
    public List<PersonalAssignmentDto.Res> getByUser(@PathVariable Long userId) {
        return personalAssignmentService.getByUser(userId);
    }

    // 직접 생성한 과제 수정
    @PutMapping("/{id}")
    public PersonalAssignmentDto.Res update(@PathVariable Long id,
                                            @RequestBody @Valid PersonalAssignmentDto.UpdateReq req) {
        return personalAssignmentService.update(id, req);
    }

    // 직접 생성한 과제 삭제
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        personalAssignmentService.delete(id);
    }
}
