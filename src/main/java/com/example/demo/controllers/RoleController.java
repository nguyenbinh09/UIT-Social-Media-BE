package com.example.demo.controllers;

import com.example.demo.dtos.requests.CreateRoleRequest;
import com.example.demo.services.RoleService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/posts")
@AllArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearerAuth")
public class RoleController {
    private final RoleService roleService;

    @PostMapping("/createRole")
    public ResponseEntity<?> createRole(@RequestBody CreateRoleRequest roleRequest) {
        try {
            return roleService.createRole(roleRequest);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
