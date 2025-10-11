package com.metaverse.moem.auth.controller;

// ... 기존 임포트 ...

import com.metaverse.moem.auth.dto.AuthResponseDto;
import com.metaverse.moem.auth.dto.LoginRequestDto;
import com.metaverse.moem.auth.dto.SignUpRequestDto;
import com.metaverse.moem.auth.service.UserService;
import com.metaverse.moem.auth.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;


@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserService userService;

    @PostMapping("/auth/signup")
    public ResponseEntity<String> registerUser(@Valid @RequestBody SignUpRequestDto signUpRequestDto) {
//        try {
//            userService.registerUser(signUpRequestDto);
//            return ResponseEntity.status(HttpStatus.CREATED).body("회원 가입 성공");
//        } catch (IllegalArgumentException e) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("회원 가입 중 오류가 발생했습니다.");
//        }
        try {
            userService.registerUser(signUpRequestDto);

            // **!!! DB 성공 후 문제가 발생하면 500으로 명확히 응답하도록 함 !!!**
            return ResponseEntity.status(HttpStatus.CREATED).body("회원 가입 성공");

        } catch (IllegalArgumentException e) {
            // DTO 문제 외의 로직 오류 (예: 중복)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());

        } catch (RuntimeException e) { // 💡 RuntimeException 추가
            // DB 성공 후 발생한 NullPointer, IndexOutOfBounds 등 런타임 예외를 잡음
            System.err.println("DB 성공 후 Runtime 예외 발생: " + e.getMessage());
            e.printStackTrace(); // 콘솔에 스택 트레이스 출력
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("회원 가입 후처리 중 오류가 발생했습니다.");

        } catch (Exception e) {
            // 일반 예외
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("회원 가입 중 알 수 없는 오류가 발생했습니다.");
        }
    }

    @PostMapping("/auth/login")
    public ResponseEntity<AuthResponseDto> authenticateUser(@Valid @RequestBody LoginRequestDto loginRequestDto) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequestDto.getUsername(),
                            loginRequestDto.getPassword()
                    )
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String accessToken = jwtUtil.generateToken(userDetails);

            return ResponseEntity.ok(new AuthResponseDto(userDetails.getUsername(), accessToken));
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new AuthResponseDto(null, null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new AuthResponseDto(null, null));
        }
    }
}