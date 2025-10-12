package com.metaverse.moem.matching.service;

import com.metaverse.moem.matching.domain.ProjectPost;
import com.metaverse.moem.auth.domain.User;
import com.metaverse.moem.auth.repository.UserRepository;
import com.metaverse.moem.matching.dto.MatchingRequest;
import com.metaverse.moem.matching.dto.MatchingResponse;
import com.metaverse.moem.matching.repository.ProjectPostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ProjectPostService {

    private final ProjectPostRepository projectPostRepository;
    private final UserRepository userRepository;

    public ProjectPostService(ProjectPostRepository projectPostRepository, UserRepository userRepository) {
        this.projectPostRepository = projectPostRepository;
        this.userRepository = userRepository;
    }

    public MatchingResponse create(MatchingRequest req) {
        // 작성자 조회 (User 직접 조회)
        User creator = userRepository.findById(req.getCreatorId())
                    .orElseThrow(() -> new RuntimeException("작성자를 찾을 수 없습니다."));

        ProjectPost projectPost = new ProjectPost();
        projectPost.setTitle(safeTrim(req.getTitle()));
        projectPost.setIntro(safeTrim(req.getIntro()));
        projectPost.setDescription(safeTrim(req.getDescription()));
        projectPost.setTags(req.getTags() == null ? List.of() : req.getTags());
        projectPost.setDeadline(req.getDeadline().atStartOfDay());
        projectPost.setCreator(creator);
        projectPost.setWorkStyle(safeTrim(req.getWorkStyle()));
        projectPost.setContactType(safeTrim(req.getContactType()));
        projectPost.setContactValue(safeTrim(req.getContactValue()));
        projectPost.setCollaborationPeriod(safeTrim(req.getCollaborationPeriod()));
        
        // 포지션 변환
        if (req.getPositions() != null) {
            List<ProjectPost.Position> positions = req.getPositions().stream()
                    .map(pos -> {
                        ProjectPost.Position position = new ProjectPost.Position();
                        position.setRole(safeTrim(pos.getRole()));
                        position.setHeadcount(pos.getHeadcount());
                        return position;
                    })
                    .filter(pos -> pos.getRole() != null && !pos.getRole().isEmpty())
                    .toList();
            projectPost.setPositions(positions);
        }
        
        ProjectPost saved = projectPostRepository.save(projectPost);
        return toResp(saved);
    }

    @Transactional(readOnly = true)
    public List<MatchingResponse> list() {
        return projectPostRepository.findAll().stream().map(this::toResp).toList();
    }

    @Transactional(readOnly = true)
    public MatchingResponse get(Long id) {
        ProjectPost projectPost = projectPostRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("NOT_FOUND"));
        return toResp(projectPost);
    }

    @Transactional
    public MatchingResponse update(Long id, MatchingRequest req) {
        ProjectPost projectPost = projectPostRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("NOT_FOUND"));
        
        projectPost.setTitle(safeTrim(req.getTitle()));
        projectPost.setIntro(safeTrim(req.getIntro()));
        projectPost.setDescription(safeTrim(req.getDescription()));
        projectPost.setTags(req.getTags() == null ? List.of() : req.getTags());
        projectPost.setDeadline(req.getDeadline().atStartOfDay());
        projectPost.setWorkStyle(safeTrim(req.getWorkStyle()));
        projectPost.setContactType(safeTrim(req.getContactType()));
        projectPost.setContactValue(safeTrim(req.getContactValue()));
        projectPost.setCollaborationPeriod(safeTrim(req.getCollaborationPeriod()));
        
        // 포지션 변환
        if (req.getPositions() != null) {
            List<ProjectPost.Position> positions = req.getPositions().stream()
                    .map(pos -> {
                        ProjectPost.Position position = new ProjectPost.Position();
                        position.setRole(safeTrim(pos.getRole()));
                        position.setHeadcount(pos.getHeadcount());
                        return position;
                    })
                    .filter(pos -> pos.getRole() != null && !pos.getRole().isEmpty())
                    .toList();
            projectPost.setPositions(positions);
        }
        
        ProjectPost saved = projectPostRepository.save(projectPost);
        return toResp(saved);
    }

    private MatchingResponse toResp(ProjectPost projectPost) {
        List<MatchingResponse.PositionResponse> positions = projectPost.getPositions() == null ? 
                List.of() : 
                projectPost.getPositions().stream()
                        .map(pos -> new MatchingResponse.PositionResponse(pos.getRole(), pos.getHeadcount()))
                        .toList();
        
        return new MatchingResponse(
                projectPost.getId(),
                projectPost.getTitle(),
                projectPost.getIntro(),
                projectPost.getDescription(),
                projectPost.getTags(),
                projectPost.getDeadline().toLocalDate(),
                projectPost.getCreatorUsername(),
                projectPost.getWorkStyle(),
                projectPost.getContactType(),
                projectPost.getContactValue(),
                projectPost.getCollaborationPeriod(),
                positions
        );
    }

    private String safeTrim(String s) {
        return s == null ? null : s.trim();
    }

    public void delete(Long id) {
        if (!projectPostRepository.existsById(id)) {
            throw new IllegalArgumentException("NOT_FOUND");
        }
        projectPostRepository.deleteById(id);
    }
}
