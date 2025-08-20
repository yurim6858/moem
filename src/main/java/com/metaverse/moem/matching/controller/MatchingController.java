package com.metaverse.moem.matching.controller;

import com.metaverse.moem.matching.dto.MatchingRequest;
import com.metaverse.moem.matching.dto.MatchingResponse;
import com.metaverse.moem.matching.service.MatchingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/recruitments")
public class MatchingController {

    private final MatchingService matchingService;

    public MatchingController(MatchingService matchingService) {
        this.matchingService = matchingService;
    }

    @PostMapping
    public ResponseEntity<MatchingResponse> create(@RequestBody MatchingRequest req) {
        MatchingResponse res = matchingService.create(req);

        Long id = (res.id() != null) ? res.id() : null;

        return ResponseEntity
                .created(URI.create("/api/recruitments/" + id))
                .body(res);
    }

    @GetMapping
    public List<MatchingResponse> list() {
        return matchingService.list();
    }

    @GetMapping("/{id}")
    public MatchingResponse get(@PathVariable Long id) {
        return matchingService.get(id);
    }
}
