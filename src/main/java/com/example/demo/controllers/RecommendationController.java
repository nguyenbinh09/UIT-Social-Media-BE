package com.example.demo.controllers;

import com.example.demo.services.RecommendationService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/recommendations")
@AllArgsConstructor
@PreAuthorize("hasRole('STUDENT') or hasRole('LECTURER')")
@SecurityRequirement(name = "bearerAuth")
public class RecommendationController {
    private final RecommendationService recommendationService;

    @GetMapping("/recommendUsers")
    public ResponseEntity<?> recommendUsers() {
        try {
            return ResponseEntity.ok(recommendationService.recommendUsers());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
