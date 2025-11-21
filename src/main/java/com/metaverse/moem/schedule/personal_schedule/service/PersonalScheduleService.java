package com.metaverse.moem.schedule.personal_schedule.service;

import com.metaverse.moem.assignment.personal_assignment.domain.PersonalAssignment;
import com.metaverse.moem.assignment.personal_assignment.repository.PersonalAssignmentRepository;
import com.metaverse.moem.schedule.personal_schedule.dto.PersonalScheduleDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PersonalScheduleService {

    private final PersonalAssignmentRepository personalAssignmentRepository;

    public List<PersonalScheduleDto.Res> getSchedules(Long userId) {
        LocalDateTime now = LocalDateTime.now();

        List<PersonalAssignment> assignments = personalAssignmentRepository.findAllByUserId(userId);
        
        // 만료된 개인 생성 과제를 별도로 처리 (ConcurrentModificationException 방지)
        List<PersonalAssignment> toDelete = new ArrayList<>();
        for (PersonalAssignment assignment : assignments) {
            if (assignment.isUserCreated() && assignment.getDueAt() != null && assignment.getDueAt().isBefore(now)) {
                toDelete.add(assignment);
            }
        }
        
        // 삭제는 별도 트랜잭션에서 처리하는 것이 좋지만, 현재는 조회 시점에만 처리
        // 주의: 조회 메서드에서 삭제하는 것은 좋은 패턴이 아닙니다. 별도 스케줄러나 배치 작업으로 처리하는 것이 좋습니다.
        if (!toDelete.isEmpty()) {
            assignments.removeAll(toDelete);
        }

        return assignments.stream()
                .map(personalAssignment -> new PersonalScheduleDto.Res(
                        personalAssignment.getId(),
                        personalAssignment.getTitle(),
                        personalAssignment.getDescription(),
                        personalAssignment.getDueAt(),
                        personalAssignment.isUserCreated(),
                        calculateStatus(personalAssignment.getDueAt(), personalAssignment.getCreatedAt(), now)
                ))
                .toList();
    }

    private PersonalScheduleDto.AssignmentStatus calculateStatus(LocalDateTime dueAt, LocalDateTime createdAt, LocalDateTime now) {
        if (dueAt == null || createdAt == null) {
            return PersonalScheduleDto.AssignmentStatus.알수없음;
        }

        if (createdAt.toLocalDate().isEqual(now.toLocalDate())) {
            return PersonalScheduleDto.AssignmentStatus.신규;
        }

        if (dueAt.isBefore(now)) {
            return PersonalScheduleDto.AssignmentStatus.마감지남;
        }

        if (now.plusDays(3).isAfter(dueAt)) {
            return PersonalScheduleDto.AssignmentStatus.마감임박;
        }

        return PersonalScheduleDto.AssignmentStatus.여유;
    }

}
