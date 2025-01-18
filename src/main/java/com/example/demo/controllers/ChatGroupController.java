package com.example.demo.controllers;

import com.example.demo.dtos.requests.CreateChatGroupRequest;
import com.example.demo.dtos.requests.UpdateChatGroupRequest;
import com.example.demo.services.ChatGroupService;
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
@RequestMapping("/api/chat_groups")
@AllArgsConstructor
@PreAuthorize("hasRole('STUDENT') or hasRole('LECTURER')")
@SecurityRequirement(name = "bearerAuth")
public class ChatGroupController {
    private final ChatGroupService chatGroupService;
    private final ObjectMapper objectMapper;

    @PostMapping("/createChatGroup")
    public ResponseEntity<?> createChatGroup(@RequestBody CreateChatGroupRequest createChatGroupRequest) {
        try {
            return chatGroupService.createChatGroup(createChatGroupRequest);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{chatGroupId}/addGroupChatMember")
    public ResponseEntity<?> addGroupChatMember(@PathVariable Long chatGroupId, @RequestParam List<String> memberIds) {
        try {
            return chatGroupService.addGroupChatMember(chatGroupId, memberIds);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{chatGroupId}/removeGroupChatMember")
    public ResponseEntity<?> removeGroupChatMember(@PathVariable Long chatGroupId, @RequestParam List<String> memberIds) {
        try {
            return chatGroupService.removeGroupChatMember(chatGroupId, memberIds);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping(value = "/{chatGroupId}/updateChatGroup", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateChatGroup(@PathVariable Long chatGroupId, @RequestParam String updateChatGroupRequestString, @RequestPart(required = false) MultipartFile avatarImage) {
        try {
            UpdateChatGroupRequest updateChatGroupRequest = objectMapper.readValue(updateChatGroupRequestString, UpdateChatGroupRequest.class);
            return chatGroupService.updateChatGroup(chatGroupId, updateChatGroupRequest, avatarImage);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/getChatGroups")
    public ResponseEntity<?> getChatGroups(@RequestParam int page, @RequestParam int size) {
        try {
            return chatGroupService.getChatGroups(page, size);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/getChatGroupById/{chatGroupId}")
    public ResponseEntity<?> getChatGroupById(@PathVariable Long chatGroupId) {
        try {
            return chatGroupService.getChatGroupById(chatGroupId);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{chatGroupId}/getMembers")
    public ResponseEntity<?> getMembers(@PathVariable Long chatGroupId) {
        try {
            return chatGroupService.getMembers(chatGroupId);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
