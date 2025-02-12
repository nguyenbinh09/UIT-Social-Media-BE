package com.example.demo.controllers;

import com.example.demo.dtos.requests.AccountStatusRequest;
import com.example.demo.dtos.requests.AdminRegisterRequest;
import com.example.demo.dtos.requests.AdminUpdateUserRequest;
import com.example.demo.dtos.requests.CreatePostRequest;
import com.example.demo.enums.PostStatus;
import com.example.demo.services.PostService;
import com.example.demo.services.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@AllArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearerAuth")
public class AdminController {
    private final UserService userService;
    private final PostService postService;
    private final ObjectMapper objectMapper;

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

    @PostMapping("/registerAdmin")
    public ResponseEntity<?> registerAdmin(@RequestBody AdminRegisterRequest adminRegisterRequest) {
        try {
            return userService.registerAdmin(adminRegisterRequest);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping(value = "/createNotificationPost", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createNotificationPost(@RequestParam String postRequestString, @RequestPart(required = false) List<MultipartFile> mediaFiles) {
        try {
            CreatePostRequest postRequest = objectMapper.readValue(postRequestString, CreatePostRequest.class);
            return postService.createNotificationPost(postRequest, mediaFiles);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
