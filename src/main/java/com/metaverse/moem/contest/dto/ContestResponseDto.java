package com.metaverse.moem.contest.dto;

import com.metaverse.moem.contest.domain.Contest;
import lombok.Getter;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Getter
public class ContestResponseDto {
    private Long id;
    private String title;
    private String host;
    private String category;
    private LocalDate deadline;
    private long dDay;
    private String sourceUrl;

    public ContestResponseDto(Contest contest) {
        this.id = contest.getId();
        this.title = contest.getTitle();
        this.host = contest.getHost();
        this.category = contest.getCategory();
        this.deadline = contest.getDeadline();
        this.sourceUrl = contest.getSourceUrl();

        this.dDay = ChronoUnit.DAYS.between(LocalDate.now(), contest.getDeadline());
    }
}