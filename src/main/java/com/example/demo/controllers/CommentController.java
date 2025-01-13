package com.example.demo.controllers;

import com.example.demo.dtos.requests.CreateCommentRequest;
import com.example.demo.services.CommentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.AllArgsConstructor;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
@AllArgsConstructor
@PreAuthorize("hasRole('STUDENT') or hasRole('LECTURER')")
@SecurityRequirement(name = "bearerAuth")
public class CommentController {
    private final CommentService commentService;
    private final ObjectMapper objectMapper;

    @PostMapping(value = "/createComment", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createComment(@RequestParam String commentRequestString, @RequestPart(required = false) List<MultipartFile> mediaFiles) {
        try {
            CreateCommentRequest commentRequest = objectMapper.readValue(commentRequestString, CreateCommentRequest.class);
            return commentService.createComment(commentRequest, mediaFiles);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/getComments")
    public ResponseEntity<?> getComments(@RequestParam Long postId, @RequestParam int page, @RequestParam int size, @RequestParam PagedResourcesAssembler assembler) {
        try {
            return commentService.getCommentsWithReplies(postId, page, size, assembler);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{commentId}/getReplies")
    public ResponseEntity<?> getReplies(@PathVariable Long commentId, @RequestParam int page, @RequestParam int size, @RequestParam PagedResourcesAssembler assembler) {
        try {
            return commentService.getReplies(commentId, page, size, assembler);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
