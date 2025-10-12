package com.metaverse.moem.matching.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class UserResponse {
    private Long id;
    private String email;
    private String username;
    private String intro;
    private List<String> skills;
    private String contactType;
    private String contactValue;
    private String workStyle;
    private String collaborationPeriod;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
