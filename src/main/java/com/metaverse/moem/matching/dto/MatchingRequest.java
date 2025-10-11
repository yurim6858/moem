package com.metaverse.moem.matching.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class MatchingRequest {
    private String title;
    private String intro;
    private String description;
    private List<String> tags;
    private LocalDate deadline;
    private Long creatorId; // 작성자 ID
    private String username; // 호환성을 위해 유지
    private String workStyle;
    private String contactType;
    private String contactValue;
    private String collaborationPeriod;
    private List<PositionRequest> positions;

    @Getter
    @Setter
    public static class PositionRequest {
        private String role;
        private Integer headcount;
    }

    public MatchingRequest() {}
}
