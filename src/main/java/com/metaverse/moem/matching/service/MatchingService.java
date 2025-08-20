package com.metaverse.moem.matching.service;

import com.metaverse.moem.matching.domain.Matching;
import com.metaverse.moem.matching.dto.MatchingRequest;
import com.metaverse.moem.matching.dto.MatchingResponse;
import com.metaverse.moem.matching.repository.MatchingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class MatchingService {

    private final MatchingRepository repo;

    public MatchingService(MatchingRepository repo) {
        this.repo = repo;
    }

    public MatchingResponse create(MatchingRequest req) {
        Matching m = new Matching();
        m.setTitle(safeTrim(req.getTitle()));
        m.setDescription(safeTrim(req.getDesc()));
        m.setTags(req.getTags() == null ? List.of() : req.getTags());
        m.setDeadline(req.getDeadline().atStartOfDay());
        m.setUsername(safeTrim(req.getUsername()));
        Matching saved = repo.save(m);
        return toResp(saved);
    }

    @Transactional(readOnly = true)
    public List<MatchingResponse> list() {
        return repo.findAll().stream().map(this::toResp).toList();
    }

    @Transactional(readOnly = true)
    public MatchingResponse get(Long id) {
        Matching m = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("NOT_FOUND"));
        return toResp(m);
    }

    private MatchingResponse toResp(Matching m) {
        return new MatchingResponse(
                m.getId(),
                m.getTitle(),
                m.getDescription(),
                m.getTags(),
                m.getDeadline().toLocalDate(),
                m.getUsername()
        );
    }

    private String safeTrim(String s) {
        return s == null ? null : s.trim();
    }
}
