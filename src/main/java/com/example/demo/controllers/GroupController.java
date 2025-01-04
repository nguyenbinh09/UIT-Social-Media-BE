package com.example.demo.controllers;

import com.example.demo.dtos.requests.CreateGroupRequest;
import com.example.demo.dtos.requests.UpdateGroupRequest;
import com.example.demo.enums.InvitationStatus;
import com.example.demo.enums.MembershipRequestStatus;
import com.example.demo.services.GroupService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/groups")
@AllArgsConstructor
@PreAuthorize("hasRole('USER')")
@SecurityRequirement(name = "bearerAuth")
public class GroupController {
    private final GroupService groupService;

    @PostMapping("/create")
    public ResponseEntity<?> createGroup(@RequestBody CreateGroupRequest createGroupRequest) {
        try {
            return groupService.createGroup(createGroupRequest);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/update/{groupId}")
    public ResponseEntity<?> updateGroup(@PathVariable Long groupId, @RequestBody UpdateGroupRequest updateGroupRequest) {
        try {
            return groupService.updateGroup(groupId, updateGroupRequest);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{groupId}/invite")
    public ResponseEntity<?> inviteUser(@PathVariable Long groupId, @RequestParam String userId) {
        try {
            return groupService.inviteUser(groupId, userId);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/invitation/{invitationId}/respond")
    public ResponseEntity<?> respondToInvitation(@PathVariable Long invitationId, @RequestParam InvitationStatus status) {
        try {
            return groupService.respondToInvitation(invitationId, status);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{groupId}/addMember")
    public ResponseEntity<?> addMember(@PathVariable Long groupId, @RequestParam String userId) {
        try {
            return groupService.addMember(groupId, userId);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/join/{groupId}")
    public ResponseEntity<?> joinGroup(@PathVariable Long groupId) {
        try {
            return groupService.joinGroup(groupId);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("requests/{requestId}/handle")
    public ResponseEntity<?> handleRequest(@PathVariable Long requestId, @RequestParam MembershipRequestStatus status) {
        try {
            return groupService.handleRequest(requestId, status);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/{groupId}/members")
    public ResponseEntity<?> getMembers(@PathVariable Long groupId, int page, int size) {
        try {
            return groupService.getMembers(groupId, page, size);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{groupId}/leave")
    public ResponseEntity<?> leaveGroup(@PathVariable Long groupId) {
        try {
            return groupService.leaveGroup(groupId);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/getGroups/{userId}")
    public ResponseEntity<?> getGroups(@PathVariable String userId) {
        try {
            return groupService.getGroups(userId);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/group/{groupId}")
    public ResponseEntity<?> getGroup(@PathVariable Long groupId) {
        try {
            return groupService.getGroup(groupId);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
