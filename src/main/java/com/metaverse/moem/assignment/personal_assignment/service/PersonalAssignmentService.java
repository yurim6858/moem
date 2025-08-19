package com.metaverse.moem.assignment.personal_assignment.service;

import com.metaverse.moem.assignment.personal_assignment.domain.PersonalAssignment;
import com.metaverse.moem.assignment.personal_assignment.dto.PersonalAssignmentDto;
import com.metaverse.moem.assignment.personal_assignment.repository.PersonalAssignmentRepository;
import com.metaverse.moem.assignment.team_assignment.domain.TeamAssignment;
import com.metaverse.moem.assignment.team_assignment.repository.TeamAssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PersonalAssignmentService {

    private final PersonalAssignmentRepository personalAssignmentRepository;
    private final TeamAssignmentRepository teamAssignmentRepository;

    // 개인 과제 생성
    public PersonalAssignmentDto.Res create(PersonalAssignmentDto.CreateReq req) {
        PersonalAssignment assignment;

        if (req.teamAssignmentId() != null) {
            // team_assignment 기반
            TeamAssignment teamAssignment = teamAssignmentRepository.findById(req.teamAssignmentId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 팀 과제입니다."));

            assignment = PersonalAssignment.builder()
                    .teamAssignment(teamAssignment)
                    .userId(req.userId())
                    .title(teamAssignment.getTitle())
                    .description((teamAssignment.getDescription()))
                    .dueAt(teamAssignment.getDueAt())
                    .userCreated(false)
                    .build();

        } else {
            // user created
            assignment = PersonalAssignment.builder()
                    .userId(req.userId())
                    .title(req.title())
                    .description(req.description())
                    .dueAt(req.dueAt())
                    .userCreated(true)
                    .build();
        }

        return toRes(personalAssignmentRepository.save(assignment));
    }

    // 전체 조회 (사용자 기반)
    public List<PersonalAssignmentDto.Res> getByUser(Long userId) {
        return personalAssignmentRepository.findAllByUserId(userId).stream()
                .map(this::toRes)
                .toList();
    }

    // 직접 생성한 과제만 수정
    public PersonalAssignmentDto.Res update(Long id, PersonalAssignmentDto.UpdateReq req) {
        PersonalAssignment assignment = personalAssignmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("과제가 존재하지 않습니다."));

        if (!assignment.isUserCreated()) {
            throw new UnsupportedOperationException("팀 과제 기반 개인 과제는 수정할 수 없습니다.");
        }

        assignment.update(req.title(), req.description(), req.dueAt());
        return toRes(assignment);
    }

    // 직접 생성한 과제만 삭제
    public void delete(Long id) {
        PersonalAssignment assignment = personalAssignmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("과제가 존재하지 않습니다."));

        if (!assignment.isUserCreated()) {
            throw new UnsupportedOperationException("팀 과제 기반 개인 과제는 삭제할 수 없습니다.");
        }

        personalAssignmentRepository.delete(assignment);
    }

    private PersonalAssignmentDto.Res toRes(PersonalAssignment a) {
        return new PersonalAssignmentDto.Res(
                a.getId(),
                a.getTeamAssignment() != null ? a.getTeamAssignment().getId() : null,
                a.getUserId(),
                a.getTitle(),
                a.getDescription(),
                a.getDueAt(),
                a.isUserCreated()
        );
    }


}
