package com.example.demo.controllers;

import com.example.demo.services.SearchService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/search")
@AllArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class SearchController {

    private final SearchService searchService;

    @GetMapping
    public ResponseEntity<?> searchAll(@RequestParam String keyword) {
        try {
            Map<String, List<?>> results = searchService.searchAll(keyword);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/posts")
    public ResponseEntity<?> searchPosts(@RequestParam String keyword) {
        try {
            List<?> results = searchService.searchPosts(keyword);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/users")
    public ResponseEntity<?> searchUsers(@RequestParam String keyword) {
        try {
            List<?> results = searchService.searchUsers(keyword);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/groups")
    public ResponseEntity<?> searchGroups(@RequestParam String keyword) {
        try {
            List<?> results = searchService.searchGroups(keyword);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
