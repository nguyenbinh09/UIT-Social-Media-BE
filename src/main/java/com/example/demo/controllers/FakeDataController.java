package com.example.demo.controllers;

import com.example.demo.services.DataGenerationService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/data-generation")
@AllArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearerAuth")
public class FakeDataController {
    private final DataGenerationService dataGenerationService;

    @PostMapping("/generate/users")
    public void generateUserData() {
        dataGenerationService.generateUserData();
    }
}
