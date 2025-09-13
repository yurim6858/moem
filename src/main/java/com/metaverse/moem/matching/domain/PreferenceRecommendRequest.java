package com.metaverse.moem.matching.domain;

import com.metaverse.moem.matching.dto.TimeSlotDto;
import lombok.Builder;

import java.util.List;

@Builder
public record PreferenceRecommendRequest(
        List<String> skills,
        List<String> interests,
        List<TimeSlotDto> availability,
        MeetingPreference meetingPreference,
        String regionCode,
        Integer limit,

        Double skillWeight, Double interestWeight, Double timeWeight, Double meetingWeight, Double regionWeight
) {}
