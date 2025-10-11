package com.metaverse.moem.auth.service;

import com.metaverse.moem.auth.domain.Auth;
import com.metaverse.moem.auth.dto.AuthResponse;
import com.metaverse.moem.auth.dto.LoginRequest;
import com.metaverse.moem.auth.dto.RegisterRequest;
import com.metaverse.moem.auth.repository.AuthRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final AuthRepository authRepository;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // 이메일 중복 확인
        if (authRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("이미 존재하는 이메일입니다.");
        }

        // 사용자명 중복 확인
        if (authRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("이미 존재하는 사용자명입니다.");
        }

        // 새 사용자 생성
        Auth user = Auth.builder()
                .email(request.getEmail())
                .username(request.getUsername())
                .password(request.getPassword()) // 실제로는 암호화 필요
                .build();

        Auth savedUser = authRepository.save(user);

        // 간단한 토큰 생성 (실제로는 JWT 사용 권장)
        String token = generateSimpleToken();

        return new AuthResponse(
                token,
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getUsername(),
                "회원가입이 완료되었습니다."
        );
    }

    public AuthResponse login(LoginRequest request) {
        // 이메일로 로그인하도록 변경 (username이 중복될 수 있음)
        Auth user = authRepository.findByEmail(request.getUsername())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 이메일입니다."));

        // 비밀번호 확인 (실제로는 암호화된 비밀번호와 비교)
        if (!user.getPassword().equals(request.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }

        // 간단한 토큰 생성
        String token = generateSimpleToken();

        return new AuthResponse(
                token,
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                "로그인 성공"
        );
    }

    public Auth getUserById(Long id) {
        return authRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
    }

    public List<Auth> getAllUsers() {
        return authRepository.findAll();
    }

    private String generateSimpleToken() {
        // 실제로는 JWT 토큰을 사용해야 함
        return "token_" + UUID.randomUUID().toString().replace("-", "");
    }
}
