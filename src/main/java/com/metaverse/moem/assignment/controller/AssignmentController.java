package com.metaverse.moem.assignment.controller;

import com.metaverse.moem.assignment.dto.AssignmentDto;
import com.metaverse.moem.assignment.service.AssignmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assignments")
@RequiredArgsConstructor
public class AssignmentController {

    private final AssignmentService assignmentService;

    // 과제 할당 생성
    @PostMapping
    public AssignmentDto.Res create(@RequestBody @Valid AssignmentDto.CreateReq req) {
        return assignmentService.create(req);
    }

    // 특정 프로젝트 내 모든 할당 조회
    @GetMapping("/project/{projectId}")
    public List<AssignmentDto.Res> getByProject(@PathVariable Long projectId) {
        return assignmentService.getByProject(projectId);
    }

    // 특정 유저에게 할당된 과제만 조회
    @GetMapping("/project/{projectId}/user/{userId}")
    public List<AssignmentDto.Res> getByProjectAndUser(@PathVariable Long projectId,
                                                       @PathVariable Long userId) {
        return assignmentService.getByProjectAndUser(projectId, userId);
    }
}
