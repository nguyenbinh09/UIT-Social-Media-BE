package com.example.demo.controllers;

import com.example.demo.dtos.requests.CreateChatGroupRequest;
import com.example.demo.services.ChatGroupService;
import com.example.demo.services.MessageService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat_groups")
@AllArgsConstructor
@PreAuthorize("hasRole('USER')")
@SecurityRequirement(name = "bearerAuth")
public class ChatGroupController {
    private final ChatGroupService chatGroupService;

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
}
