package com.metaverse.moem.matching.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecommendationResponseDto {
    private UserDto baseUser;
    private List<RecommendationRequestDto> recommendations;
}
