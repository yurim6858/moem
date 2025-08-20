package com.metaverse.moem.matching.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class MatchingRequest {
    private String title;
    private String desc;
    private List<String> tags;
    private LocalDate deadline;
    private String username;

    public MatchingRequest() {}
}
