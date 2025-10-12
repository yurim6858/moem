package com.metaverse.moem.matching.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UserRequest {
    private String email;
    private String username;
    private String intro;
    private List<String> skills;
    private String contactType;
    private String contactValue;
    private String workStyle;
    private String collaborationPeriod;
}
