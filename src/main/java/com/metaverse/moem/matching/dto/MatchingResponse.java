package com.metaverse.moem.matching.dto;

import java.time.LocalDate;
import java.util.List;

public record MatchingResponse(
        Long id,
        String title,
        String intro,
        String description,
        List<String> tags,
        LocalDate deadline,
        String username,
        String workStyle,
        String contactType,
        String contactValue,
        String collaborationPeriod,
        List<PositionResponse> positions
) {
    public record PositionResponse(
            String role,
            Integer headcount
    ) {}
}
