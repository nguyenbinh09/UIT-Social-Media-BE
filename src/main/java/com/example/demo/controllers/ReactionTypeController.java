package com.example.demo.controllers;

import com.example.demo.dtos.requests.CreateReactionTypeRequest;
import com.example.demo.enums.ReactionTypeName;
import com.example.demo.models.ReactionType;
import com.example.demo.services.ReactionTypeService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reaction_types")
@AllArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearerAuth")
public class ReactionTypeController {
    private final ReactionTypeService reactionTypeService;

    @PostMapping("/create")
    public ResponseEntity<?> createReactionType(@RequestBody CreateReactionTypeRequest createReactionTypeRequest) {
        try {
            return reactionTypeService.createReactionType(createReactionTypeRequest);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
