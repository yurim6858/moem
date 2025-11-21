package com.metaverse.moem.auth.service;

import com.metaverse.moem.application.domain.Application;
import com.metaverse.moem.application.repository.ApplicationRepository;
import com.metaverse.moem.auth.domain.User;
import com.metaverse.moem.auth.domain.UserRole;
import com.metaverse.moem.auth.dto.AuthUserResponseDto;
import com.metaverse.moem.auth.dto.SignUpRequestDto;
import com.metaverse.moem.auth.repository.UserRepository;
import com.metaverse.moem.matching.domain.ProjectPost;
import com.metaverse.moem.matching.repository.ProjectPostRepository;
import com.metaverse.moem.team.domain.TeamMembers;
import com.metaverse.moem.team.repository.TeamMembersRepository;
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
    private final ProjectPostRepository projectPostRepository;
    private final ApplicationRepository applicationRepository;
    private final TeamMembersRepository teamMembersRepository;

    @Transactional
    public void registerUser(SignUpRequestDto signUpRequestDto) {
        // username 유효성 검사 (영문, 숫자, 언더스코어만 허용)
        String username = signUpRequestDto.getUsername();
        if (username != null && !username.matches("^[a-zA-Z0-9_]+$")) {
            throw new IllegalArgumentException("아이디는 영문, 숫자, 언더스코어(_)만 사용할 수 있습니다.");
        }

        if (userRepository.existsByUsername(username)) {
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

    /**
     * User 삭제 시 연관된 엔티티들을 처리합니다.
     * - ProjectPost.creator: 삭제된 사용자로 인한 ProjectPost는 삭제 처리
     * - Application.applicant: WITHDRAWN 상태로 변경
     * - TeamMembers.userId: 관련 TeamMembers 삭제 (팀에서 제거)
     * - Project.ownerId: 소유자 정보는 유지하되, 삭제된 사용자임을 표시할 수 있음
     * 
     * 주의: 실제 삭제 대신 비활성화를 권장합니다.
     */
    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. userId = " + userId));

        // 1. ProjectPost.creator 처리: 삭제된 사용자가 작성한 공고는 삭제 처리
        try {
            // ProjectPostRepository에서 creator로 조회하는 메서드가 없으므로
            // 모든 ProjectPost를 조회하여 필터링 (실제 운영 환경에서는 쿼리 최적화 필요)
            List<ProjectPost> allPosts = projectPostRepository.findByIsDeletedFalse();
            List<ProjectPost> createdPosts = allPosts.stream()
                    .filter(post -> post.getCreator() != null && post.getCreator().getId().equals(userId))
                    .collect(Collectors.toList());
            
            for (ProjectPost post : createdPosts) {
                // ProjectPost 삭제 (soft delete)
                post.setDeleted(true);
                projectPostRepository.save(post);
            }
        } catch (Exception e) {
            System.err.println("ProjectPost 처리 중 오류 발생: " + e.getMessage());
        }

        // 2. Application.applicant 처리: WITHDRAWN 상태로 변경
        try {
            List<Application> userApplications = applicationRepository.findByApplicant_Id(userId);
            for (Application application : userApplications) {
                if (application.getStatus() == Application.ApplicationStatus.PENDING) {
                    application.setStatus(Application.ApplicationStatus.WITHDRAWN);
                    applicationRepository.save(application);
                }
            }
        } catch (Exception e) {
            System.err.println("Application 처리 중 오류 발생: " + e.getMessage());
        }

        // 3. TeamMembers.userId 처리: 관련 TeamMembers 삭제
        try {
            List<TeamMembers> userMemberships = teamMembersRepository.findByUserId(userId);
            for (TeamMembers member : userMemberships) {
                // TeamMembersService의 delete 로직을 재사용하거나 직접 삭제
                // 여기서는 직접 삭제 (팀 정원 업데이트는 TeamMembersService에서 처리)
                teamMembersRepository.delete(member);
            }
        } catch (Exception e) {
            System.err.println("TeamMembers 처리 중 오류 발생: " + e.getMessage());
        }

        // 4. Project.ownerId 처리: 소유자 정보는 유지 (프로젝트는 계속 진행 가능)
        // Project.ownerId는 Long 타입이므로 삭제된 사용자임을 표시할 수 없음
        // 필요시 Project에 isOwnerDeleted 같은 플래그를 추가하거나, 
        // 삭제된 사용자의 프로젝트는 별도 관리 로직 필요

        // 실제 User 삭제 (또는 비활성화)
        // 주의: 실제 삭제는 데이터 무결성 문제를 일으킬 수 있으므로,
        // 비활성화(soft delete)를 권장합니다.
        userRepository.delete(user);
    }
}