package com.example.demo.controllers;

import com.example.demo.dtos.requests.CreatePostRequest;
import com.example.demo.dtos.requests.UpdatePostRequest;
import com.example.demo.models.Post;
import com.example.demo.services.PostService;
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
@RequestMapping("/api/posts")
@AllArgsConstructor
@PreAuthorize("hasRole('USER')")
@SecurityRequirement(name = "bearerAuth")
public class PostController {
    private final PostService postService;
    private final ObjectMapper objectMapper;

    @GetMapping("/feed")
    public ResponseEntity<?> getPostFeed(@RequestParam int page, @RequestParam int size) {
        try {
            return ResponseEntity.ok(postService.getPostFeed(page, size));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping(value = "/createPost", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createPost(@RequestParam String postRequestString, @RequestPart(required = false) List<MultipartFile> mediaFiles){
        try {
            CreatePostRequest postRequest = objectMapper.readValue(postRequestString, CreatePostRequest.class);
            return postService.createPost(postRequest, mediaFiles);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping(value = "/updatePost", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updatePost(@RequestParam String postRequestString, @RequestPart(required = false) List<MultipartFile> mediaFiles){
        try {
            UpdatePostRequest postRequest = objectMapper.readValue(postRequestString, UpdatePostRequest.class);
            return postService.updatePost(postRequest, mediaFiles);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

//    @PostMapping( value = "/createImage", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    public ResponseEntity<?> createImage(@RequestPart("mediaFiles") List<MultipartFile> files){
//        try {
//            return ResponseEntity.ok(postService.createImage(files));
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body(e.getMessage());
//        }
//    }
}
