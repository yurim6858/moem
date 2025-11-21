package com.metaverse.moem.assignment.personal_assignment.service;

import com.metaverse.moem.assignment.personal_assignment.domain.PersonalAssignment;
import com.metaverse.moem.assignment.personal_assignment.dto.PersonalAssignmentDto;
import com.metaverse.moem.assignment.personal_assignment.repository.PersonalAssignmentRepository;
import com.metaverse.moem.assignment.team_assignment.domain.TeamAssignment;
import com.metaverse.moem.assignment.team_assignment.repository.TeamAssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PersonalAssignmentService {

    private final PersonalAssignmentRepository personalAssignmentRepository;
    private final TeamAssignmentRepository teamAssignmentRepository;

    @Transactional
    public PersonalAssignmentDto.Res createFromTeam(PersonalAssignmentDto.CreateFromTeamReq req) {
        TeamAssignment teamAssignment = teamAssignmentRepository.findById(req.teamAssignmentId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 팀 과제입니다."));

        PersonalAssignment assignment = PersonalAssignment.builder()
                .teamAssignment(teamAssignment)
                .userId(req.userId())
                .title(teamAssignment.getTitle())
                .description(teamAssignment.getDescription())
                .dueAt(teamAssignment.getDueAt())
                .userCreated(false)
                .build();

        return toRes(personalAssignmentRepository.save(assignment));
    }

    @Transactional
    public PersonalAssignmentDto.Res createOwn(PersonalAssignmentDto.CreateOwnReq req) {
        PersonalAssignment assignment = PersonalAssignment.builder()
                .userId(req.userId())
                .title(req.title())
                .description(req.description())
                .dueAt(req.dueAt())
                .userCreated(true)
                .build();

        return toRes(personalAssignmentRepository.save(assignment));
    }

    public List<PersonalAssignmentDto.Res> getByUser(Long userId) {
        return personalAssignmentRepository.findAllByUserId(userId).stream()
                .map(this::toRes)
                .toList();
    }

    @Transactional
    public PersonalAssignmentDto.Res update(Long id, PersonalAssignmentDto.UpdateReq req) {
        PersonalAssignment assignment = personalAssignmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("과제가 존재하지 않습니다."));

        if (!assignment.isUserCreated()) {
            throw new UnsupportedOperationException("팀 과제 기반 개인 과제는 수정할 수 없습니다.");
        }

        assignment.update(req.title(), req.description(), req.dueAt());
        return toRes(assignment);
    }

    @Transactional
    public void delete(Long id) {
        PersonalAssignment assignment = personalAssignmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("과제가 존재하지 않습니다."));

        if (!assignment.isUserCreated()) {
            throw new UnsupportedOperationException("팀 과제 기반 개인 과제는 삭제할 수 없습니다.");
        }

        personalAssignmentRepository.delete(assignment);
    }

    private PersonalAssignmentDto.Res toRes(PersonalAssignment personalAssignment) {
        return new PersonalAssignmentDto.Res(
                personalAssignment.getId(),
                personalAssignment.getTeamAssignment() != null ? personalAssignment.getTeamAssignment().getId() : null,
                personalAssignment.getUserId(),
                personalAssignment.getTitle(),
                personalAssignment.getDescription(),
                personalAssignment.getDueAt(),
                personalAssignment.isUserCreated()
        );
    }


}
