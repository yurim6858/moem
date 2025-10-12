package com.metaverse.moem.matching.service;

import com.metaverse.moem.auth.domain.Auth;
import com.metaverse.moem.auth.repository.AuthRepository;
import com.metaverse.moem.matching.domain.UserPost;
import com.metaverse.moem.matching.dto.UserRequest;
import com.metaverse.moem.matching.dto.UserResponse;
import com.metaverse.moem.matching.repository.UserPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserPostService {

    private final UserPostRepository userPostRepository;
    private final AuthRepository authRepository;

    @Transactional
    public UserResponse createUserPost(UserRequest request) {
        // 기존 Auth 조회 또는 생성
        Auth auth = authRepository.findByEmail(request.getEmail())
                .orElseGet(() -> {
                    // Auth가 없으면 새로 생성
                    Auth newAuth = Auth.builder()
                            .email(request.getEmail())
                            .username(request.getUsername())
                            .password("default_password") // 실제로는 암호화 필요
                            .build();
                    return authRepository.save(newAuth);
                });
        
        // 이미 UserPost가 있는지 확인 (username 기준)
        if (userPostRepository.findByAuth_Username(request.getUsername()).isPresent()) {
            throw new RuntimeException("이미 등록된 사용자명입니다.");
        }

        // UserPost 생성
        UserPost userPost = UserPost.builder()
                .auth(auth)
                .intro(request.getIntro())
                .skills(request.getSkills() != null ? request.getSkills() : List.of())
                .contactType(request.getContactType())
                .contactValue(request.getContactValue())
                .workStyle(request.getWorkStyle())
                .collaborationPeriod(request.getCollaborationPeriod())
                .build();

        UserPost savedUserPost = userPostRepository.save(userPost);
        return convertToResponse(savedUserPost);
    }

    public List<UserResponse> getAllUserPosts() {
        return userPostRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public UserResponse getUserPostById(Long id) {
        UserPost userPost = userPostRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("게시물을 찾을 수 없습니다."));
        return convertToResponse(userPost);
    }

    @Transactional
    public UserResponse updateUserPost(Long id, UserRequest request) {
        UserPost userPost = userPostRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("게시물을 찾을 수 없습니다."));

        // 사용자명 중복 확인 (자신 제외)
        Auth currentAuth = userPost.getAuth();
        if (currentAuth != null && !currentAuth.getUsername().equals(request.getUsername()) && 
            userPostRepository.existsByAuth_Username(request.getUsername())) {
            throw new RuntimeException("이미 존재하는 사용자명입니다.");
        }

        // Auth 업데이트
        Auth auth = userPost.getAuth();
        auth.setEmail(request.getEmail());
        auth.setUsername(request.getUsername());
        authRepository.save(auth);

        // UserPost 업데이트
        userPost.setIntro(request.getIntro());
        userPost.setSkills(request.getSkills() != null ? request.getSkills() : List.of());
        userPost.setContactType(request.getContactType());
        userPost.setContactValue(request.getContactValue());
        userPost.setWorkStyle(request.getWorkStyle());
        userPost.setCollaborationPeriod(request.getCollaborationPeriod());

        UserPost updatedUserPost = userPostRepository.save(userPost);
        return convertToResponse(updatedUserPost);
    }

    @Transactional
    public void deleteUserPost(Long id) {
        if (!userPostRepository.existsById(id)) {
            throw new RuntimeException("게시물을 찾을 수 없습니다.");
        }
        userPostRepository.deleteById(id);
    }

    private UserResponse convertToResponse(UserPost userPost) {
        UserResponse response = new UserResponse();
        response.setId(userPost.getId());
        response.setEmail(userPost.getEmail());
        response.setUsername(userPost.getUsername());
        response.setIntro(userPost.getIntro());
        response.setSkills(userPost.getSkills());
        response.setContactType(userPost.getContactType());
        response.setContactValue(userPost.getContactValue());
        response.setWorkStyle(userPost.getWorkStyle());
        response.setCollaborationPeriod(userPost.getCollaborationPeriod());
        response.setCreatedAt(userPost.getCreatedAt());
        response.setUpdatedAt(userPost.getUpdatedAt());
        return response;
    }
}
