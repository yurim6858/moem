package com.metaverse.moem.assignment.service;

import com.metaverse.moem.assignment.domain.Assignment;
import com.metaverse.moem.assignment.dto.AssignmentDto;
import com.metaverse.moem.assignment.repository.AssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AssignmentService {

    private final AssignmentRepository assignmentRepository;

    public AssignmentDto.Res create(AssignmentDto.CreateReq req) {
        Assignment saved =  assignmentRepository.save(
            Assignment.builder()
                    .projectId(req.projectId())
                    .userId(req.userId())
                    .description(req.description())
                    .title(req.title())
                    .dueAt(req.dueAt())
                    .build()
        );

        return toRes(saved);
    }

    public List<AssignmentDto.Res> getByProjectId(Long projectId) {
        return assignmentRepository.findByProjectId(projectId).stream()
                .map(this::toRes)
                .toList();
    }

    public List<AssignmentDto.Res> getByProjectAndUser(Long projectId, Long userId) {
        return assignmentRepository.findByProjectIdAndUserId(projectId, userId).stream()
                .map(this::toRes)
                .toList();
    }

    private AssignmentDto.Res toRes(Assignment a) {
        return new AssignmentDto.Res(
                a.getId(),
                a.getProjectId(),
                a.getUserId(),
                a.getTitle(),
                a.getDescription(),
                a.getDueAt()
        );
    }
}
