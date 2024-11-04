package com.example.demo.controllers;

import com.example.demo.dtos.requests.ReactPostRequest;
import com.example.demo.services.ReactionService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reactions")
@AllArgsConstructor
@PreAuthorize("hasRole('USER')")
@SecurityRequirement(name = "bearerAuth")
public class ReactionController {
    private final ReactionService reactionService;

    @PostMapping("/reactPost")
    public ResponseEntity<?> reactPost(@RequestBody ReactPostRequest reactPostRequest) {
        try {
            return reactionService.reactPost(reactPostRequest);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/deleteReaction")
    public ResponseEntity<?> deleteReaction(@RequestParam Long postId) {
        try {
            return reactionService.deleteReaction(postId);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
