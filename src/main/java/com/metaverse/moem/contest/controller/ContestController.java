package com.metaverse.moem.contest.controller;

import com.metaverse.moem.contest.domain.Contest;
import com.metaverse.moem.contest.dto.ContestResponseDto;
import com.metaverse.moem.contest.repository.ContestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/contests")
@RequiredArgsConstructor
public class ContestController {

    private final ContestRepository contestRepository;

    @CrossOrigin(origins = "http://localhost:5173")
    @GetMapping
    public List<ContestResponseDto> getActiveContests(
            @RequestParam(defaultValue = "IT") String category, // 현재는 로직 미적용 상태
            @RequestParam(defaultValue = "10") int limit
    ) {
        List<Contest> contests = contestRepository.findAllByDeadlineAfterOrderByCreatedAtDesc(LocalDate.now());

        return contests.stream()
                .limit(limit)
                .map(ContestResponseDto::new) 
                .collect(Collectors.toList());
    }
}