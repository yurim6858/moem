package com.metaverse.moem.schedule.personal_schedule.service;

import com.metaverse.moem.assignment.personal_assignment.domain.PersonalAssignment;
import com.metaverse.moem.assignment.personal_assignment.repository.PersonalAssignmentRepository;
import com.metaverse.moem.schedule.personal_schedule.dto.PersonalScheduleDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PersonalScheduleService {

    private final PersonalAssignmentRepository personalAssignmentRepository;

    public List<PersonalScheduleDto.Res> getSchedules(Long userId) {
        LocalDateTime now =  LocalDateTime.now();

        List<PersonalAssignment> assignments = personalAssignmentRepository.findAllByUserId(userId);

        // auto-delete : userCreated == true && 마감 지남
        assignments.removeIf(personalAssignment -> {
            if (personalAssignment.isUserCreated() && personalAssignment.getDueAt().isBefore(now)){
                personalAssignmentRepository.delete(personalAssignment);
                return true;
            }
            return false;
        });

        return assignments.stream()
                .map(personalAssignment -> new PersonalScheduleDto.Res(
                        personalAssignment.getId(),
                        personalAssignment.getTitle(),
                        personalAssignment.getDescription(),
                        personalAssignment.getDueAt(),
                        personalAssignment.isUserCreated(),
                        calculateStatus(personalAssignment.getDueAt(),personalAssignment.getCreatedAt(), now)
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
