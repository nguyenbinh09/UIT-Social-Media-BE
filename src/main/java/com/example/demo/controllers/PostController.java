package com.example.demo.controllers;

import com.example.demo.dtos.requests.CreatePostRequest;
import com.example.demo.dtos.requests.UpdatePostRequest;
import com.example.demo.enums.InvitationStatus;
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
    public ResponseEntity<?> createPost(@RequestParam String postRequestString, @RequestPart(required = false) List<MultipartFile> mediaFiles) {
        try {
            CreatePostRequest postRequest = objectMapper.readValue(postRequestString, CreatePostRequest.class);
            return postService.createPost(postRequest, mediaFiles);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping(value = "/updatePost", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updatePost(@RequestParam String postRequestString, @RequestPart(required = false) List<MultipartFile> mediaFiles) {
        try {
            UpdatePostRequest postRequest = objectMapper.readValue(postRequestString, UpdatePostRequest.class);
            return postService.updatePost(postRequest, mediaFiles);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping(value = "/groups/{groupId}/createGroupPost", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createGroupPost(@PathVariable Long groupId, @RequestParam String postRequestString, @RequestPart(required = false) List<MultipartFile> mediaFiles) {
        try {
            CreatePostRequest postRequest = objectMapper.readValue(postRequestString, CreatePostRequest.class);
            return postService.createGroupPost(groupId, postRequest, mediaFiles);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/reviewPost")
    public ResponseEntity<?> reviewPost(@RequestParam Long postId, @RequestParam boolean isApproved) {
        try {
            return postService.reviewPostInGroup(postId, isApproved);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<?> deletePost(@PathVariable Long postId) {
        try {
            return postService.deletePost(postId);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{postId}")
    public ResponseEntity<?> getPost(@PathVariable Long postId) {
        try {
            return ResponseEntity.ok(postService.getPost(postId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/groups/{groupId}")
    public ResponseEntity<?> getGroupPosts(@PathVariable Long groupId, @RequestParam int page, @RequestParam int size) {
        try {
            return ResponseEntity.ok(postService.getGroupPosts(groupId, page, size));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/GetPostByUser")
    public ResponseEntity<?> getPostByUser(@RequestParam int page, @RequestParam int size) {
        try {
            return postService.getPostByUser(page, size);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/savePost")
    public ResponseEntity<?> savePost(@RequestParam Long postId) {
        try {
            return postService.savePost(postId);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/unsavedPost")
    public ResponseEntity<?> unsavedPost(@RequestParam Long postId) {
        try {
            return postService.unsavePost(postId);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/getSavedPosts")
    public ResponseEntity<?> getSavedPosts(@RequestParam int page, @RequestParam int size) {
        try {
            return postService.getSavedPosts(page, size);
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
