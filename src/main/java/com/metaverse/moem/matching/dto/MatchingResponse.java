package com.metaverse.moem.matching.dto;

import java.time.LocalDate;
import java.util.List;

public record MatchingResponse(
        Long id,
        String title,
        String desc,
        List<String> tags,
        LocalDate deadline,
        String username
) {}
