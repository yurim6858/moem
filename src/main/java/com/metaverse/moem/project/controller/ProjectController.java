package com.metaverse.moem.project.controller;

import com.metaverse.moem.project.domain.ProjectType;
import com.metaverse.moem.project.dto.ProjectDto;
import com.metaverse.moem.project.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ProjectController.class);

    @PostMapping
    public ProjectDto.Res create(@RequestBody @Valid ProjectDto.CreateReq req) {
        return projectService.create(req);
    }

    @GetMapping("/{id}")
    public Optional<ProjectDto.Res> get(@PathVariable Long id) {
        return projectService.get(id);
    }

    @GetMapping("/owner")
    public List<ProjectDto.Res> listByOwner(
            @RequestParam ProjectType type,
            @RequestParam Long ownerId
    ) {
        return projectService.listByOwner(type, ownerId);
    }

    @PutMapping("/{id}")
    public Optional<ProjectDto.Res> update(@PathVariable Long id,
                                           @RequestBody @Valid ProjectDto.UpdateReq req) {
        return projectService.update(id, req);
    }

    @DeleteMapping("/{id}")
    public boolean delete(@PathVariable Long id) {
        return projectService.delete(id);
    }

}
