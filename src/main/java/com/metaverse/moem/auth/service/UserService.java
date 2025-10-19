package com.metaverse.moem.auth.service;

import com.metaverse.moem.auth.domain.User;
import com.metaverse.moem.auth.domain.UserRole;
import com.metaverse.moem.auth.dto.AuthUserResponseDto;
import com.metaverse.moem.auth.dto.SignUpRequestDto;
import com.metaverse.moem.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void registerUser(SignUpRequestDto signUpRequestDto) {
        if (userRepository.existsByUsername(signUpRequestDto.getUsername())) {
            throw new IllegalArgumentException("Username 사용자 계정이 사용중입니다.");
        }

        if (userRepository.existsByEmail(signUpRequestDto.getEmail())) {
            throw new IllegalArgumentException("Email 사용자 이메일이 사용중입니다.");
        }

        User user = new User(
                signUpRequestDto.getUsername(),
                signUpRequestDto.getNickname(),
                passwordEncoder.encode(signUpRequestDto.getPassword()),
                signUpRequestDto.getEmail(),
                UserRole.ROLE_USER
        );

        userRepository.save(user);
    }

    // 모든 사용자 목록 조회
    public List<AuthUserResponseDto> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(user -> new AuthUserResponseDto(
                        user.getId(),
                        user.getUsername(),
                        user.getNickname(),
                        user.getEmail(),
                        user.getUserRole(),
                        user.getCreatedAt(),
                        user.getModifiedAt()
                ))
                .collect(Collectors.toList());
    }
}