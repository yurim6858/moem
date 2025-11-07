package com.metaverse.moem.application.controller;

import com.metaverse.moem.application.domain.Application;
import com.metaverse.moem.application.dto.ApplicationRequest;
import com.metaverse.moem.application.dto.ApplicationResponse;
import com.metaverse.moem.application.dto.ApplicationStatusRequest;
import com.metaverse.moem.application.service.ApplicationService;
import com.metaverse.moem.common.dto.ErrorResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;

    @PostMapping
    public ResponseEntity<?> apply(@RequestBody ApplicationRequest request) {
        try {
            // username이 body에 포함되어 있지 않으면 에러
            if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ErrorResponse.of("INVALID_REQUEST", "username이 필요합니다."));
            }
            ApplicationResponse response = applicationService.apply(request, request.getUsername());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ErrorResponse.of("INVALID_REQUEST", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ErrorResponse.of("BAD_REQUEST", e.getMessage()));
        }
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<?> getApplicationsByProject(@PathVariable Long projectId) {
        try {
            List<ApplicationResponse> applications = applicationService.getApplicationsByProject(projectId);
            return ResponseEntity.ok(applications);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.of("PROJECT_NOT_FOUND", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ErrorResponse.of("BAD_REQUEST", e.getMessage()));
        }
    }

    // 포지션별로 그룹화된 지원자 목록 조회
    @GetMapping("/project/{projectId}/by-position")
    public ResponseEntity<?> getApplicationsByProjectGroupedByPosition(@PathVariable Long projectId) {
        try {
            com.metaverse.moem.application.dto.ApplicationByPositionResponse.Res response = 
                applicationService.getApplicationsByProjectGroupedByPosition(projectId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.of("PROJECT_NOT_FOUND", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ErrorResponse.of("BAD_REQUEST", e.getMessage()));
        }
    }

    @GetMapping("/user")
    public ResponseEntity<?> getApplicationsByUser(@RequestHeader("X-Username") String username) {
        try {
            List<ApplicationResponse> applications = applicationService.getApplicationsByUser(username);
            return ResponseEntity.ok(applications);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.of("USER_NOT_FOUND", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ErrorResponse.of("BAD_REQUEST", e.getMessage()));
        }
    }

    // 지원서 상태 변경 (RESTful 스타일 - Request Body 사용)
    @PatchMapping("/{applicationId}/status")
    public ResponseEntity<?> updateApplicationStatus(@PathVariable Long applicationId,
                                                     @RequestBody @Valid ApplicationStatusRequest request) {
        try {
            ApplicationResponse response = applicationService.updateApplicationStatus(applicationId, request.getStatus());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.of("APPLICATION_NOT_FOUND", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ErrorResponse.of("BAD_REQUEST", e.getMessage()));
        }
    }

    // 지원서 상태 변경 (하위 호환성을 위해 유지, deprecated)
    @PutMapping("/{applicationId}/status")
    @Deprecated
    public ResponseEntity<?> updateApplicationStatusLegacy(@PathVariable Long applicationId,
                                                           @RequestParam Application.ApplicationStatus status) {
        try {
            ApplicationResponse response = applicationService.updateApplicationStatus(applicationId, status);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.of("APPLICATION_NOT_FOUND", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ErrorResponse.of("BAD_REQUEST", e.getMessage()));
        }
    }

    @DeleteMapping("/{applicationId}")
    public ResponseEntity<?> withdrawApplication(@PathVariable Long applicationId, 
                                                 @RequestHeader("X-Username") String username) {
        try {
            applicationService.withdrawApplication(applicationId, username);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.of("APPLICATION_NOT_FOUND", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ErrorResponse.of("BAD_REQUEST", e.getMessage()));
        }
    }

    @PostMapping("/{applicationId}/approve")
    public ResponseEntity<?> approveAndAddToTeam(@PathVariable Long applicationId, 
                                                  @RequestHeader("X-Username") String username) {
        try {
            ApplicationResponse response = applicationService.approveAndAddToTeam(applicationId, username);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.of("APPLICATION_NOT_FOUND", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ErrorResponse.of("BAD_REQUEST", e.getMessage()));
        }
    }

    // 지원서 승인 및 초대 발송 (하위 호환성을 위해 유지, deprecated)
    @PostMapping("/{applicationId}/invitation")
    @Deprecated
    public ResponseEntity<?> approveAndSendInvitation(@PathVariable Long applicationId, 
                                                      @RequestHeader("X-Username") String username) {
        try {
            ApplicationResponse response = applicationService.approveAndAddToTeam(applicationId, username);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.of("APPLICATION_NOT_FOUND", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ErrorResponse.of("BAD_REQUEST", e.getMessage()));
        }
    }

    // 지원서 승인 및 초대 발송 (하위 호환성을 위해 유지, deprecated)
    @PostMapping("/{applicationId}/approve-and-invite")
    @Deprecated
    public ResponseEntity<?> approveAndSendInvitationLegacy(@PathVariable Long applicationId, 
                                                            @RequestHeader("X-Username") String username) {
        try {
            ApplicationResponse response = applicationService.approveAndAddToTeam(applicationId, username);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse.of("APPLICATION_NOT_FOUND", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(ErrorResponse.of("BAD_REQUEST", e.getMessage()));
        }
    }
}

