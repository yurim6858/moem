package com.metaverse.moem.application.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApplicationRequest {
    private Long projectId;
    private String message;
    private String appliedPosition; // 지원한 포지션
    private String username; // 지원자 username (헤더 대신 body에 포함)
}

