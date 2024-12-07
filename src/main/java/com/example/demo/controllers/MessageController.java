package com.example.demo.controllers;

import com.example.demo.dtos.requests.SendGroupMessageRequest;
import com.example.demo.dtos.requests.SendMessageRequest;
import com.example.demo.services.MessageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
@AllArgsConstructor
@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
@SecurityRequirement(name = "bearerAuth")
public class MessageController {
    private final MessageService messageService;
    private final ObjectMapper objectMapper;

    @PostMapping(value = "/one-to-one/send", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> sendOneToOneMessage(@RequestParam String sendMessageString, @RequestPart(required = false) List<MultipartFile> mediaFiles) {
        try {
            SendMessageRequest sendMessageRequest = objectMapper.readValue(sendMessageString, SendMessageRequest.class);
            return messageService.sendOneToOneMessage(sendMessageRequest, mediaFiles);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{messageId}/mark-as-read")
    public ResponseEntity<?> markMessageAsRead(@PathVariable Long messageId) {
        try {
            return messageService.markMessageAsRead(messageId);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/approve/{conversationId}")
    public ResponseEntity<?> approvePendingMessage(@PathVariable Long conversationId, @RequestParam Boolean approve) {
        try {
            return messageService.approvePendingMessage(conversationId, approve);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping(value = "/group/send", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> sendGroupMessaage(@RequestParam String sendGroupMessageString, @RequestPart(required = false) List<MultipartFile> mediaFiles) {
        try {
            SendGroupMessageRequest sendGroupMessageRequest = objectMapper.readValue(sendGroupMessageString, SendGroupMessageRequest.class);
            return messageService.sendGroupMessage(sendGroupMessageRequest, mediaFiles);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    
}