package com.metaverse.moem.application.dto;

import com.metaverse.moem.application.domain.Application;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApplicationStatusRequest {
    @NotNull(message = "상태는 필수입니다.")
    private Application.ApplicationStatus status;
}

