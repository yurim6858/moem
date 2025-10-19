package com.metaverse.moem.auth.dto;

import com.metaverse.moem.auth.domain.UserRole;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class AuthUserResponseDto {
    private Long id;
    private String username;
    private String nickname;
    private String email;
    private UserRole userRole;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    public AuthUserResponseDto(Long id, String username, String nickname, String email, UserRole userRole, LocalDateTime createdAt, LocalDateTime modifiedAt) {
        this.id = id;
        this.username = username;
        this.nickname = nickname;
        this.email = email;
        this.userRole = userRole;
        this.createdAt = createdAt;
        this.modifiedAt = modifiedAt;
    }
}
