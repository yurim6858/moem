package com.metaverse.moem.team.controller;

import com.metaverse.moem.common.dto.ErrorResponse;
import com.metaverse.moem.team.dto.TeamMemberLeaveRequestDto;
import com.metaverse.moem.team.service.TeamMemberLeaveRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teams/{teamId}/leave-requests")
@RequiredArgsConstructor
public class TeamMemberLeaveRequestController {

    private final TeamMemberLeaveRequestService leaveRequestService;

    // 팀원 나가기 요청 생성
    @PostMapping("/members/{memberId}")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<?> createLeaveRequest(
            @PathVariable Long teamId,
            @PathVariable Long memberId,
            @RequestBody @Valid TeamMemberLeaveRequestDto.CreateReq req,
            @RequestHeader("X-Username") String username) {
        try {
            TeamMemberLeaveRequestDto.Res response = 
                leaveRequestService.createLeaveRequest(teamId, memberId, req, username);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ErrorResponse.of("BAD_REQUEST", e.getMessage()));
        }
    }

    // 팀의 나가기 요청 목록 조회 (리더용)
    @GetMapping
    public ResponseEntity<?> getLeaveRequests(
            @PathVariable Long teamId,
            @RequestHeader("X-Username") String username) {
        try {
            List<TeamMemberLeaveRequestDto.Res> requests = 
                leaveRequestService.getLeaveRequestsByTeam(teamId, username);
            return ResponseEntity.ok(requests);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ErrorResponse.of("BAD_REQUEST", e.getMessage()));
        }
    }

    // 팀의 대기중인 나가기 요청 목록 조회 (리더용)
    @GetMapping("/pending")
    public ResponseEntity<?> getPendingLeaveRequests(
            @PathVariable Long teamId,
            @RequestHeader("X-Username") String username) {
        try {
            List<TeamMemberLeaveRequestDto.Res> requests = 
                leaveRequestService.getPendingLeaveRequestsByTeam(teamId, username);
            return ResponseEntity.ok(requests);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ErrorResponse.of("BAD_REQUEST", e.getMessage()));
        }
    }

    // 나가기 요청 승인/거절 (리더용)
    @PutMapping("/{leaveRequestId}/respond")
    public ResponseEntity<?> respondToLeaveRequest(
            @PathVariable Long teamId,
            @PathVariable Long leaveRequestId,
            @RequestBody @Valid TeamMemberLeaveRequestDto.RespondReq req,
            @RequestHeader("X-Username") String username) {
        try {
            TeamMemberLeaveRequestDto.Res response = 
                leaveRequestService.respondToLeaveRequest(leaveRequestId, req, username);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ErrorResponse.of("BAD_REQUEST", e.getMessage()));
        }
    }
}

