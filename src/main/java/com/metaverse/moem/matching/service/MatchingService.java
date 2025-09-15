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
        Matching matching = new Matching();
        matching.setTitle(safeTrim(req.getTitle()));
        matching.setIntro(safeTrim(req.getIntro()));
        matching.setDescription(safeTrim(req.getDescription()));
        matching.setTags(req.getTags() == null ? List.of() : req.getTags());
        matching.setDeadline(req.getDeadline().atStartOfDay());
        matching.setUsername(safeTrim(req.getUsername()));
        matching.setWorkStyle(safeTrim(req.getWorkStyle()));
        matching.setContactType(safeTrim(req.getContactType()));
        matching.setContactValue(safeTrim(req.getContactValue()));
        matching.setCollaborationPeriod(safeTrim(req.getCollaborationPeriod()));
        
        // 포지션 변환
        if (req.getPositions() != null) {
            List<Matching.Position> positions = req.getPositions().stream()
                    .map(pos -> {
                        Matching.Position position = new Matching.Position();
                        position.setRole(safeTrim(pos.getRole()));
                        position.setHeadcount(pos.getHeadcount());
                        return position;
                    })
                    .filter(pos -> pos.getRole() != null && !pos.getRole().isEmpty())
                    .toList();
            matching.setPositions(positions);
        }
        
        Matching saved = repo.save(matching);
        return toResp(saved);
    }

    @Transactional(readOnly = true)
    public List<MatchingResponse> list() {
        return repo.findAll().stream().map(this::toResp).toList();
    }

    @Transactional(readOnly = true)
    public MatchingResponse get(Long id) {
        Matching matching = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("NOT_FOUND"));
        return toResp(matching);
    }

    @Transactional
    public MatchingResponse update(Long id, MatchingRequest req) {
        Matching matching = repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("NOT_FOUND"));
        
        matching.setTitle(safeTrim(req.getTitle()));
        matching.setIntro(safeTrim(req.getIntro()));
        matching.setDescription(safeTrim(req.getDescription()));
        matching.setTags(req.getTags() == null ? List.of() : req.getTags());
        matching.setDeadline(req.getDeadline().atStartOfDay());
        matching.setUsername(safeTrim(req.getUsername()));
        matching.setWorkStyle(safeTrim(req.getWorkStyle()));
        matching.setContactType(safeTrim(req.getContactType()));
        matching.setContactValue(safeTrim(req.getContactValue()));
        matching.setCollaborationPeriod(safeTrim(req.getCollaborationPeriod()));
        
        // 포지션 변환
        if (req.getPositions() != null) {
            List<Matching.Position> positions = req.getPositions().stream()
                    .map(pos -> {
                        Matching.Position position = new Matching.Position();
                        position.setRole(safeTrim(pos.getRole()));
                        position.setHeadcount(pos.getHeadcount());
                        return position;
                    })
                    .filter(pos -> pos.getRole() != null && !pos.getRole().isEmpty())
                    .toList();
            matching.setPositions(positions);
        }
        
        Matching saved = repo.save(matching);
        return toResp(saved);
    }

    private MatchingResponse toResp(Matching matching) {
        List<MatchingResponse.PositionResponse> positions = matching.getPositions() == null ? 
                List.of() : 
                matching.getPositions().stream()
                        .map(pos -> new MatchingResponse.PositionResponse(pos.getRole(), pos.getHeadcount()))
                        .toList();
        
        return new MatchingResponse(
                matching.getId(),
                matching.getTitle(),
                matching.getIntro(),
                matching.getDescription(),
                matching.getTags(),
                matching.getDeadline().toLocalDate(),
                matching.getUsername(),
                matching.getWorkStyle(),
                matching.getContactType(),
                matching.getContactValue(),
                matching.getCollaborationPeriod(),
                positions
        );
    }

    private String safeTrim(String s) {
        return s == null ? null : s.trim();
    }

    public void delete(Long id) {
        if (!repo.existsById(id)) {
            throw new IllegalArgumentException("NOT_FOUND");
        }
        repo.deleteById(id);
    }
}
