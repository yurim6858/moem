package com.metaverse.moem.application.controller;

import com.metaverse.moem.application.domain.Application;
import com.metaverse.moem.application.dto.ApplicationRequest;
import com.metaverse.moem.application.dto.ApplicationResponse;
import com.metaverse.moem.application.service.ApplicationService;
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
    public ResponseEntity<ApplicationResponse> apply(@RequestBody ApplicationRequest request, 
                                                     @RequestHeader("X-Username") String username) {
        try {
            ApplicationResponse response = applicationService.apply(request, username);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<ApplicationResponse>> getApplicationsByProject(@PathVariable Long projectId) {
        try {
            List<ApplicationResponse> applications = applicationService.getApplicationsByProject(projectId);
            return ResponseEntity.ok(applications);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/user")
    public ResponseEntity<List<ApplicationResponse>> getApplicationsByUser(@RequestHeader("X-Username") String username) {
        try {
            List<ApplicationResponse> applications = applicationService.getApplicationsByUser(username);
            return ResponseEntity.ok(applications);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{applicationId}/status")
    public ResponseEntity<ApplicationResponse> updateApplicationStatus(@PathVariable Long applicationId, 
                                                                      @RequestParam Application.ApplicationStatus status) {
        try {
            ApplicationResponse response = applicationService.updateApplicationStatus(applicationId, status);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{applicationId}")
    public ResponseEntity<Void> withdrawApplication(@PathVariable Long applicationId, 
                                                   @RequestHeader("X-Username") String username) {
        try {
            applicationService.withdrawApplication(applicationId, username);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{applicationId}/approve-and-invite")
    public ResponseEntity<ApplicationResponse> approveAndSendInvitation(@PathVariable Long applicationId, 
                                                                      @RequestHeader("X-Username") String username) {
        try {
            ApplicationResponse response = applicationService.approveAndSendInvitation(applicationId, username);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}

