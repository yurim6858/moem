package com.metaverse.moem.team.dto;

import com.metaverse.moem.auth.domain.User;
import com.metaverse.moem.auth.repository.UserRepository;
import com.metaverse.moem.team.domain.Role;
import com.metaverse.moem.team.domain.TeamMembers;
import lombok.Builder;

public class TeamMembersDto {

    public record CreateReq(Long userId) {}

    public record UpdateReq(Role role) {}

    @Builder
    public record Res(
            Long id,
            Long teamId,
            Long userId,
            String role,  // Enum 대신 String으로 변경하여 안전성 확보
            String username,  // 사용자명 추가 (프론트엔드에서 필요할 수 있음)
            String email,     // 이메일 추가 (프론트엔드에서 필요할 수 있음)
            String createdAt,
            String updatedAt
    ) {
        // UserRepository를 파라미터로 받는 버전 (사용자 정보 조회용)
        public static Res from(TeamMembers members, UserRepository userRepository) {
            if (members == null) {
                return Res.builder()
                        .id(0L)
                        .teamId(null)
                        .userId(0L)
                        .role("MEMBER")
                        .username("")
                        .email("")
                        .createdAt("")
                        .updatedAt("")
                        .build();
            }
            
            // userId로 사용자 정보 조회
            String username = "";
            String email = "";
            if (members.getUserId() != null) {
                try {
                    User user = userRepository.findById(members.getUserId()).orElse(null);
                    if (user != null) {
                        username = user.getUsername() != null ? user.getUsername() : "";
                        email = user.getEmail() != null ? user.getEmail() : "";
                    }
                } catch (Exception e) {
                    // 사용자 조회 실패 시 빈 문자열 유지
                }
            }
            
            return fromWithUserInfo(members, username, email);
        }
        
        // 기존 from 메서드는 내부적으로 호출 (하위 호환성)
        public static Res from(TeamMembers members) {
            return fromWithUserInfo(members, "", "");
        }
        
        private static Res fromWithUserInfo(TeamMembers members, String username, String email) {
            if (members == null) {
                return Res.builder()
                        .id(0L)
                        .teamId(null)
                        .userId(0L)
                        .role("MEMBER")
                        .username(username != null ? username : "")
                        .email(email != null ? email : "")
                        .createdAt("")
                        .updatedAt("")
                        .build();
            }
            
            // 모든 필드에 대해 안전한 기본값 제공
            try {
                // team 정보 가져오기 (LAZY 로딩 안전성)
                Long teamId = null;
                try {
                    if (members.getTeam() != null) {
                        teamId = members.getTeam().getId();
                    }
                } catch (Exception e) {
                    // LAZY 로딩 실패 시 null 유지
                    teamId = null;
                }
                
                return Res.builder()
                        .id(members.getId() != null ? members.getId() : 0L)
                        .teamId(teamId)
                        .userId(members.getUserId() != null ? members.getUserId() : 0L)
                        .role(members.getRole() != null ? members.getRole().name() : "MEMBER")  // Enum을 String으로 변환
                        .username(username != null ? username : "")
                        .email(email != null ? email : "")
                        .createdAt(members.getCreatedAt() != null ? members.getCreatedAt().toString() : "")
                        .updatedAt(members.getUpdatedAt() != null ? members.getUpdatedAt().toString() : "")
                        .build();
            } catch (Exception e) {
                // 변환 실패 시 기본값 반환
                return Res.builder()
                        .id(0L)
                        .teamId(null)
                        .userId(members.getUserId() != null ? members.getUserId() : 0L)
                        .role("MEMBER")
                        .username(username != null ? username : "")
                        .email(email != null ? email : "")
                        .createdAt("")
                        .updatedAt("")
                        .build();
            }
        }
    }

}
