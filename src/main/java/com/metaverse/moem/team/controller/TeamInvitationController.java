package com.metaverse.moem.team.controller;

import com.metaverse.moem.team.dto.TeamInvitationDto;
import com.metaverse.moem.team.service.TeamInvitationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
public class TeamInvitationController {

    private final TeamInvitationService teamInvitationService;

    // 팀 초대 발송
    @PostMapping("/{teamId}/invitations")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<TeamInvitationDto.Res> sendInvitation(
            @PathVariable Long teamId,
            @RequestBody @Valid TeamInvitationDto.CreateReq request,
            @RequestHeader("X-Username") String username) {
        try {
            TeamInvitationDto.Res response = teamInvitationService.sendInvitation(teamId, request, username);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // 내가 받은 초대 목록 조회
    @GetMapping("/invitations/my")
    public ResponseEntity<List<TeamInvitationDto.ListRes>> getMyInvitations(
            @RequestHeader("X-Username") String username) {
        try {
            List<TeamInvitationDto.ListRes> invitations = teamInvitationService.getMyInvitations(username);
            return ResponseEntity.ok(invitations);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // 내가 받은 대기중인 초대 목록 조회
    @GetMapping("/invitations/my/pending")
    public ResponseEntity<List<TeamInvitationDto.ListRes>> getMyPendingInvitations(
            @RequestHeader("X-Username") String username) {
        try {
            List<TeamInvitationDto.ListRes> invitations = teamInvitationService.getMyPendingInvitations(username);
            return ResponseEntity.ok(invitations);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // 초대 상세 조회
    @GetMapping("/invitations/{invitationId}")
    public ResponseEntity<TeamInvitationDto.Res> getInvitation(
            @PathVariable Long invitationId,
            @RequestHeader("X-Username") String username) {
        try {
            TeamInvitationDto.Res invitation = teamInvitationService.getInvitation(invitationId, username);
            return ResponseEntity.ok(invitation);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // 초대 응답 (수락/거절)
    @PutMapping("/invitations/{invitationId}/respond")
    public ResponseEntity<TeamInvitationDto.Res> respondToInvitation(
            @PathVariable Long invitationId,
            @RequestBody @Valid TeamInvitationDto.RespondReq request,
            @RequestHeader("X-Username") String username) {
        try {
            TeamInvitationDto.Res response = teamInvitationService.respondToInvitation(invitationId, request, username);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // 팀의 초대 목록 조회 (팀 관리자용)
    @GetMapping("/{teamId}/invitations")
    public ResponseEntity<List<TeamInvitationDto.Res>> getTeamInvitations(@PathVariable Long teamId) {
        try {
            List<TeamInvitationDto.Res> invitations = teamInvitationService.getTeamInvitations(teamId);
            return ResponseEntity.ok(invitations);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // 초대 취소 (팀 관리자용)
    @DeleteMapping("/invitations/{invitationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> cancelInvitation(
            @PathVariable Long invitationId,
            @RequestHeader("X-Username") String username) {
        try {
            teamInvitationService.cancelInvitation(invitationId, username);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
