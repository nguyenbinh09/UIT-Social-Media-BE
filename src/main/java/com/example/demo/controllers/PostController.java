package com.example.demo.controllers;

import com.example.demo.dtos.requests.CreatePostRequest;
import com.example.demo.models.Post;
import com.example.demo.services.PostService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
@AllArgsConstructor
@PreAuthorize("hasRole('USER')")
@SecurityRequirement(name = "bearerAuth")
public class PostController {
    private final PostService postService;

    @GetMapping("/feed")
    public ResponseEntity<?> getPostFeed(@RequestParam int page, @RequestParam int size) {
        try {
            return ResponseEntity.ok(postService.getPostFeed(page, size));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/createPost")
    public ResponseEntity<?> createPost(@RequestBody CreatePostRequest postRequest) {
        try {
            return postService.createPost(postRequest);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
