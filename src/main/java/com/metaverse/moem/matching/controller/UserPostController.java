package com.metaverse.moem.matching.controller;

import com.metaverse.moem.matching.dto.UserRequest;
import com.metaverse.moem.matching.dto.UserResponse;
import com.metaverse.moem.matching.service.UserPostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user-profiles")
@RequiredArgsConstructor
public class UserPostController {

    private final UserPostService userPostService;

    @PostMapping
    public ResponseEntity<UserResponse> createUserPost(@RequestBody UserRequest request) {
        try {
            UserResponse response = userPostService.createUserPost(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUserPosts() {
        try {
            List<UserResponse> users = userPostService.getAllUserPosts();
            return ResponseEntity.ok(users);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserPostById(@PathVariable Long id) {
        try {
            UserResponse user = userPostService.getUserPostById(id);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUserPost(@PathVariable Long id, @RequestBody UserRequest request) {
        try {
            UserResponse response = userPostService.updateUserPost(id, request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUserPost(@PathVariable Long id) {
        try {
            userPostService.deleteUserPost(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
