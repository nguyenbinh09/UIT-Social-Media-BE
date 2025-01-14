package com.example.demo.controllers;

import com.example.demo.dtos.requests.AccountStatusRequest;
import com.example.demo.dtos.requests.AdminUpdateUserRequest;
import com.example.demo.enums.PostStatus;
import com.example.demo.services.PostService;
import com.example.demo.services.UserService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@AllArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {
    private final UserService userService;
    private final PostService postService;

    @PutMapping("/{userId}/status")
    public ResponseEntity<?> updateAccountStatus(@PathVariable String userId, @RequestBody AccountStatusRequest accountStatusRequest) {
        try {
            return userService.updateAccountStatus(userId, accountStatusRequest);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/updateInfoUser/{userId}")
    public ResponseEntity<?> updateUser(@PathVariable String userId, @RequestBody AdminUpdateUserRequest userRequest) {
        try {
            return userService.updateInfoUser(userId, userRequest);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/getPendingPosts")
    public ResponseEntity<?> getPendingPosts(@RequestParam int page, @RequestParam int size) {
        try {
            return postService.getPendingPosts(page, size);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/approvePost/{postId}")
    public ResponseEntity<?> approvePost(@PathVariable Long postId) {
        try {
            return postService.approvePost(postId, PostStatus.APPROVED);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/rejectPost/{postId}")
    public ResponseEntity<?> rejectPost(@PathVariable Long postId, @RequestBody String rejectionReason) {
        try {
            return postService.rejectPost(postId, PostStatus.REJECTED, rejectionReason);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
