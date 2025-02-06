package com.example.demo.controllers;

import com.example.demo.services.ChatbotService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chatbot")
@AllArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class ChatbotController {
    private final ChatbotService chatbotService;

    @PostMapping("/send-message")
    public ResponseEntity<?> sendMessageToChatbot(@RequestBody String content) {
        try {
            return chatbotService.sendMessageToChatbot(content);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/conversation")
    public ResponseEntity<?> getConversation(@RequestParam int page, @RequestParam int size) {
        try {
            return chatbotService.getChatbotConversation(page, size);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
