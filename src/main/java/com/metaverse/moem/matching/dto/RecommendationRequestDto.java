package com.metaverse.moem.matching.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RecommendationRequestDto {
    private Long userId;
    private String name;
    private List<String> skills;
    private List<String> interests;
    private double score;
}
