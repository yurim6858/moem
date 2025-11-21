package com.metaverse.moem.auth.filter;

import com.metaverse.moem.auth.util.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays; // 💡 추가
import java.util.List; // 💡 추가

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    // 💡 JWT 검증을 건너뛸 공개 경로 목록 정의
    private static final List<String> EXCLUDE_URLS = Arrays.asList(
            "/api/auth/signup",
            "/api/auth/login",
            "/api/auth/reissue"
            // /api/match/** 경로는 토큰이 없으면 AnonymousUser로 처리되므로 필터에서 건너뛰지 않아도 됩니다.
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestUri = request.getRequestURI();
        String requestMethod = request.getMethod();

        // 💡 OPTIONS 요청(CORS preflight)은 JWT 검증을 건너뛰고 즉시 통과
        if ("OPTIONS".equalsIgnoreCase(requestMethod)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 💡 1. EXCLUDE_URLS에 해당하는 경로라면 JWT 검증을 건너뛰고 다음 필터로 즉시 진행
        if (EXCLUDE_URLS.contains(requestUri)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. HTTP 요청 헤더에서 Authorization이름의 키의 값을 가져옴
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;

        System.out.println("JWT Filter: 요청 URL: " + request.getRequestURI());
        System.out.println("JWT Filter: Authorization 헤더: " + authHeader);

        // 3. Authorization 헤더가 없거나 "Bearer "로 시작하지 않으면 JWT가 없는 요청으로 간주(미인증)하고 다음 필터로 진행
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("JWT Filter: Authorization 헤더 없음 또는 Bearer 형식 아님");
            filterChain.doFilter(request, response);
            return;
        }

        // 4. "Bearer " 접두사 (7자)를 제거하고 순수한 JWT 토큰을 추출
        jwt = authHeader.substring(7);

        try {
            // 5. JWT 토큰에서 사용자 이름(계정명 *Username)을 추출
            username = jwtUtil.extractUsername(jwt);
            System.out.println("JWT Filter: 추출된 사용자명: " + username);
        } catch (RuntimeException e) { // JwtUtil에서 발생하는 JwtException(RuntimeException) 처리
            // JWT 토큰 파싱 또는 검증 중 오류가 발생하면 401 Unauthorized 에러 응답
            System.out.println("JWT Filter: 토큰 검증 실패: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // HTTP 401
            response.getWriter().write(e.getMessage()); // 에러 메시지 반환 (디버깅용)
            return;
        }

        // 6. 사용자 이름이 존재하고, Spring Security Context에 아직 인증 정보가 없는 경우에만 인증 진행
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // 7. UserDetailsService를 통해 사용자 계정(이름 *Username)을 기반으로 UserDetails 객체를 로드
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            // 8. 토큰이 유효한지 검증 (사용자 이름 일치, 토큰 만료 여부)
            if (jwtUtil.validateToken(jwt, userDetails)) {
                System.out.println("JWT Filter: 토큰 검증 성공, 인증 설정");

                // 9. JWT 토큰에서 권한 추출 및 변환
                Claims claims = jwtUtil.extractAllClaims(jwt);
                String role = (String) claims.get("role");
                System.out.println("JWT Filter: 토큰에서 추출한 권한: " + role);

                // 권한을 Spring Security GrantedAuthority로 변환
                GrantedAuthority authority = new SimpleGrantedAuthority(role);

                // 10. 토큰이 유효하면 Spring Security의 인증 토큰을 생성
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, // 인증된 사용자 주체 (UserDetails 객체)
                        null,        // 자격 증명 (비밀번호 등 - 여기서는 이미 토큰으로 인증되었으므로 null)
                        java.util.List.of(authority) // JWT 토큰에서 추출한 권한 사용
                );

                // 11. 요청에 대한 웹 인증 세부 정보(IP 주소, 세션 ID 등)를 설정
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 12. SecurityContextHolder에 인증 토큰을 설정
                SecurityContextHolder.getContext().setAuthentication(authToken);
            } else {
                System.out.println("JWT Filter: 토큰 검증 실패");
            }
        }
        // 13. 다음 필터 또는 서블릿으로 요청을 전달
        filterChain.doFilter(request, response);
    }
}