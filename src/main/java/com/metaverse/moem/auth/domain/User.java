package com.metaverse.moem.auth.domain;

import com.metaverse.moem.common.domain.TimeStamped;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "user")
public class User extends TimeStamped {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String nickname;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "user_role")
    @Enumerated(EnumType.STRING)
    private UserRole userRole;

    public User(String username, String nickname, String password, String email, UserRole userRole) {
        this.username = username;
        this.nickname = nickname;
        this.password = password;
        this.email = email;
        this.userRole = userRole;
    }
}