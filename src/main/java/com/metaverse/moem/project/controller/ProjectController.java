package com.metaverse.moem.project.controller;

import com.metaverse.moem.auth.domain.User;
import com.metaverse.moem.auth.repository.UserRepository;
import com.metaverse.moem.project.dto.ProjectDto;
import com.metaverse.moem.project.service.ProjectService;
import com.metaverse.moem.project.domain.ProjectType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;
    private final UserRepository userRepository;

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
    public ResponseEntity<ProjectDto.Res> get(@PathVariable Long id,
                                             @RequestHeader(value = "X-Username", required = false) String username) {
        // 사용자 인증이 있는 경우 시작된 프로젝트 확인
        if (username != null && !username.trim().isEmpty()) {
            Optional<ProjectDto.Res> project = projectService.getStartedProject(id, username);
            if (project.isPresent()) {
                return ResponseEntity.ok(project.get());
            }
        }
        
        // 일반 프로젝트 조회 (public)
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

    // 내가 속한 프로젝트 목록 조회 (프로젝트 시작 후)
    @GetMapping("/my")
    public ResponseEntity<List<ProjectDto.Res>> getMyProjects(
            @RequestHeader(value = "X-Username", required = false) String username) {
        if (username == null || username.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + username));
        
        List<ProjectDto.Res> myProjects = projectService.getMyProjects(user.getId());
        return ResponseEntity.ok(myProjects);
    }

    // 프로젝트 종료
    @PutMapping("/{id}/end")
    public ResponseEntity<ProjectDto.Res> endProject(
            @PathVariable Long id,
            @RequestHeader(value = "X-Username", required = false) String username) {
        
        if (username == null || username.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + username));
        
        ProjectDto.Res project = projectService.endProject(id, user.getId());
        return ResponseEntity.ok(project);
    }
}
