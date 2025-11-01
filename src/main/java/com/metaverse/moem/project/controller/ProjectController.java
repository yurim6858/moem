package com.metaverse.moem.project.controller;

import com.metaverse.moem.project.dto.ProjectDto;
import com.metaverse.moem.project.service.ProjectService;
import com.metaverse.moem.project.domain.ProjectType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    public ResponseEntity<ProjectDto.Res> create(@RequestBody @Valid ProjectDto.CreateReq req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(projectService.create(req));
    }

    @GetMapping("/public")
    public ResponseEntity<List<ProjectDto.Res>> listPublic(
            @RequestParam(required = false) ProjectType type,
            @RequestParam(required = false) String status,
            @RequestParam(name = "query", required = false) String query
    ) {
        var condition = new ProjectDto.SearchCondition(type, status, query);
        return ResponseEntity.ok(projectService.listPublic(condition));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectDto.Res> get(@PathVariable Long id) {
        return projectService.get(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


    @PutMapping("/{id}")
    public ResponseEntity<ProjectDto.Res> update(@PathVariable Long id,
                                                 @RequestBody @Valid ProjectDto.UpdateReq req) {
        return projectService.update(id, req)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        return projectService.delete(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}
