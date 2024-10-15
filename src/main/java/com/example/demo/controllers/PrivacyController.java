package com.example.demo.controllers;

import com.example.demo.dtos.requests.CreatePrivacyRequest;
import com.example.demo.services.PrivacyService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/privacy")
@AllArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearerAuth")
public class PrivacyController {
    private final PrivacyService privacyService;

    @PostMapping("/create")
    public ResponseEntity<?> createPrivacy(@RequestBody CreatePrivacyRequest createPrivacyRequest) {
        try {
            return privacyService.createPrivacy(createPrivacyRequest);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
