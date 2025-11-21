package com.metaverse.moem.matching.dto;

import lombok.Builder;

@Builder
public record TimeSlotDto(
        int dayOfWeek,
        int startMin,
        int endMin
) {}
