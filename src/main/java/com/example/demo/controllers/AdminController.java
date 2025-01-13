package com.example.demo.controllers;

import com.example.demo.dtos.requests.AccountStatusRequest;
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

    @PutMapping("/{userId}/status")
    public ResponseEntity<?> updateAccountStatus(@PathVariable String userId, @RequestBody AccountStatusRequest accountStatusRequest) {
        try {
            return userService.updateAccountStatus(userId, accountStatusRequest);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
