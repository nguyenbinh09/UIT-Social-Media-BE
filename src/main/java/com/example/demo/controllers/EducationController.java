package com.example.demo.controllers;

import com.example.demo.dtos.requests.LoginUserRequest;
import com.example.demo.services.EducationService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/educations")
@AllArgsConstructor
@PreAuthorize("hasRole('USER')")
@SecurityRequirement(name = "bearerAuth")
public class EducationController {
    private final EducationService educationService;

    @PostMapping("/loginToEducationSystem")
    public ResponseEntity<?> loginToUniversity(@RequestBody LoginUserRequest request) {
        try {
            return educationService.loginToUniversity(request.getUsername(), request.getPassword());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Login failed: " + e.getMessage());
        }
    }

    @GetMapping("/fetchStudentProfile")
    public ResponseEntity<?> fetchStudentProfile(@RequestParam String token) {
        try {
            return educationService.fetchStudentProfile(token);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to fetch student profile: " + e.getMessage());
        }
    }

    @GetMapping("/fetchScore")
    public ResponseEntity<?> fetchScore(@RequestParam String token) {
        try {
            return educationService.fetchScore(token);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to fetch student score: " + e.getMessage());
        }
    }

    @GetMapping("/fetchSchedule")
    public ResponseEntity<?> fetchSchedule(@RequestParam String token, @RequestParam int hocky, @RequestParam int namhoc) {
        try {
            return educationService.fetchSchedule(token, hocky, namhoc);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to fetch student schedule: " + e.getMessage());
        }
    }

    @GetMapping("/fetchExamSchedule")
    public ResponseEntity<?> fetchExamSchedule(@RequestParam String token, @RequestParam String examType, @RequestParam int hocky, @RequestParam int namhoc) {
        try {
            return educationService.fetchExamSchedule(token, examType, hocky, namhoc);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to fetch student exam schedule: " + e.getMessage());
        }
    }

    @GetMapping("/fetchNotification")
    public ResponseEntity<?> fetchNotification(@RequestParam String token, @RequestParam int page, @RequestParam int size) {
        try {
            return educationService.fetchNotification(token, page, size);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to fetch student notification: " + e.getMessage());
        }
    }
}
