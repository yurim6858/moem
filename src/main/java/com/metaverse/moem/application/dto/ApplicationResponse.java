package com.metaverse.moem.application.dto;

import com.metaverse.moem.application.domain.Application;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ApplicationResponse {
    private Long id;
    private Long projectId;
    private String applicantUsername;
    private String message;
    private String appliedPosition; // 지원한 포지션
    private Application.ApplicationStatus status;
    private LocalDateTime appliedAt;
    private LocalDateTime updatedAt;
}

