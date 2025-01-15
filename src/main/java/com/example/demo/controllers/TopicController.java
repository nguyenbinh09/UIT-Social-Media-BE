package com.example.demo.controllers;

import com.example.demo.dtos.requests.CreateTopicRequest;
import com.example.demo.services.TopicService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/topics")
@AllArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearerAuth")
public class TopicController {
    private final TopicService topicService;

    @PostMapping("/createTopic")
    public ResponseEntity<?> createTopic(@RequestBody CreateTopicRequest createTopicRequest) {
        try {
            return topicService.createTopic(createTopicRequest);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/getTopics")
    public ResponseEntity<?> getTopics() {
        try {
            return topicService.getTopics();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/getTopic/{id}")
    public ResponseEntity<?> getTopic(@PathVariable Long id) {
        try {
            return topicService.getTopic(id);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/updateTopic/{id}")
    public ResponseEntity<?> updateTopic(@PathVariable Long id, @RequestBody CreateTopicRequest createTopicRequest) {
        try {
            return topicService.updateTopic(id, createTopicRequest);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/deleteTopic/{id}")
    public ResponseEntity<?> deleteTopic(@PathVariable Long id) {
        try {
            return topicService.deleteTopic(id);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
