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
        assignments.removeIf(a -> {
            if (a.isUserCreated() && a.getDueAt().isBefore(now)){
                personalAssignmentRepository.delete(a);
                return true;
            }
            return false;
        });

        return assignments.stream()
                .map(a -> new PersonalScheduleDto.Res(
                        a.getId(),
                        a.getTitle(),
                        a.getDescription(),
                        a.getDueAt(),
                        a.isUserCreated(),
                        calculateStatus(a.getDueAt(),a.getCreatedAt(), now)
                ))
                .toList();
    }

    private PersonalScheduleDto.AssignmentStatus calculateStatus(LocalDateTime dueAt, LocalDateTime createdAt, LocalDateTime now) {
        if (createdAt != null && createdAt.toLocalDate().isEqual(now.toLocalDate())) {
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
