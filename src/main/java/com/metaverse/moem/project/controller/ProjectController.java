package com.metaverse.moem.project.controller;

import com.metaverse.moem.project.domain.ProjectType;
import com.metaverse.moem.project.dto.ProjectDto;
import com.metaverse.moem.project.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    // 생성
    @PostMapping
    public ProjectDto.Res create(@RequestBody @Valid ProjectDto.CreateReq req) {
        return projectService.create(req);
    }

    // single 조회
    @GetMapping("/{id}")
    public Optional<ProjectDto.Res> get(@PathVariable Long id) {
        return projectService.get(id);
    }

    // 소유자별 조회
    @GetMapping("/owner")
    public List<ProjectDto.Res> listByOwner(
            @RequestParam ProjectType type,
            @RequestParam Long ownerId
    ) {
        return projectService.listByOwner(type, ownerId);
    }

    // 수정
    @PutMapping("/{id}")
    public Optional<ProjectDto.Res> update(@PathVariable Long id,
                                           @RequestBody @Valid ProjectDto.UpdateReq req) {
        return projectService.update(id, req);
    }

    // 삭제
    @DeleteMapping("/{id}")
    public boolean delete(@PathVariable Long id) {
        return projectService.delete(id);
    }


}
