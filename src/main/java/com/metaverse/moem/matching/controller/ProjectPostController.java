package com.metaverse.moem.matching.controller;

import com.metaverse.moem.matching.dto.MatchingRequest;
import com.metaverse.moem.matching.dto.MatchingResponse;
import com.metaverse.moem.matching.service.ProjectPostService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/project-posts")
public class ProjectPostController {

    private final ProjectPostService projectPostService;

    public ProjectPostController(ProjectPostService projectPostService) {
        this.projectPostService = projectPostService;
    }

    @PostMapping
    public ResponseEntity<MatchingResponse> create(@RequestBody MatchingRequest req) {
        try {
            System.out.println("=== ProjectPostController: 요청 받음 ===");
            System.out.println("ProjectPostController: creatorId: " + req.getCreatorId());
            System.out.println("ProjectPostController: username: " + req.getUsername());
            System.out.println("ProjectPostController: title: " + req.getTitle());
            
            MatchingResponse res = projectPostService.create(req);

            Long id = (res.id() != null) ? res.id() : null;

            return ResponseEntity
                    .created(URI.create("/api/project-posts/" + id))
                    .body(res);
        } catch (Exception e) {
            System.err.println("=== ProjectPostController: 에러 발생 ===");
            System.err.println("ProjectPostController: 에러 메시지: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping
    public List<MatchingResponse> list() {
        return projectPostService.list();
    }

    @GetMapping("/{id}")
    public MatchingResponse get(@PathVariable Long id) {
        return projectPostService.get(id);
    }

    @PutMapping("/{id}")
    public MatchingResponse update(@PathVariable Long id, @RequestBody MatchingRequest req) {
        return projectPostService.update(id, req);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        projectPostService.delete(id);
        return ResponseEntity.noContent().build(); // 204
    }
}
