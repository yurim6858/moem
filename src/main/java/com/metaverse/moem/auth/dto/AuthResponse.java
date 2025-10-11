package com.metaverse.moem.auth.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthResponse {
    private String token;
    private Long userId;
    private String email;
    private String username;
    private String message;

    public AuthResponse(String token, Long userId, String email, String username, String message) {
        this.token = token;
        this.userId = userId;
        this.email = email;
        this.username = username;
        this.message = message;
    }
}
