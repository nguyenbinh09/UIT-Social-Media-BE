package com.example.demo.services;

import com.example.demo.dtos.requests.CreateChatGroupRequest;
import com.example.demo.dtos.requests.CreateGroupRequest;
import com.example.demo.dtos.requests.UpdateChatGroupRequest;
import com.example.demo.dtos.responses.ChatGroupResponse;
import com.example.demo.models.ChatGroup;
import com.example.demo.models.ChatGroupMember;
import com.example.demo.models.User;
import com.example.demo.repositories.ChatGroupRepository;
import com.example.demo.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@AllArgsConstructor
public class ChatGroupService {
    private final ChatGroupRepository chatGroupRepository;
    private final UserRepository userRepository;

    @Transactional
    public ResponseEntity<?> createChatGroup(CreateChatGroupRequest createChatGroupRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User creator = (User) authentication.getPrincipal();

        if (createChatGroupRequest.getMemberIds().isEmpty() || createChatGroupRequest.getMemberIds().size() < 2) {
            throw new RuntimeException("Group must have at least three members");
        }

        ChatGroup chatGroup = new ChatGroup();
        chatGroup.setName(createChatGroupRequest.getChatGroupName());

        ChatGroupMember creatorMember = new ChatGroupMember();
        creatorMember.setChatGroup(chatGroup);
        creatorMember.setUser(creator);
        creatorMember.setIsAdmin(true);
        chatGroup.getMembers().add(creatorMember);

        for (String userId : createChatGroupRequest.getMemberIds()) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            if (user.getId().equals(creator.getId())) {
                throw new RuntimeException("You cannot add yourself to the group");
            }
            ChatGroupMember member = new ChatGroupMember();
            member.setChatGroup(chatGroup);
            member.setUser(user);
            member.setIsAdmin(false);
            chatGroup.getMembers().add(member);
        }

        chatGroupRepository.save(chatGroup);
        return ResponseEntity.ok("Chat group created successfully");
    }

    @Transactional
    public ResponseEntity<?> addGroupChatMember(Long chatGroupId, List<String> memberIds) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        ChatGroup chatGroup = chatGroupRepository.findById(chatGroupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        if (!chatGroup.getIsInvited()) {
            boolean isAdmin = chatGroup.getMembers().stream()
                    .anyMatch(member -> member.getUser().equals(currentUser) && member.getIsAdmin());
            if (!isAdmin) {
                throw new RuntimeException("You are not authorized to add members to this group");
            }
        }

        for (String userId : memberIds) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (chatGroup.getMembers().stream().anyMatch(member -> member.getUser().getId().equals(user.getId()))) {
                return ResponseEntity.badRequest().body("User is already a member of this group");
            }

            boolean alreadyMember = chatGroup.getMembers().stream()
                    .anyMatch(member -> member.getUser().equals(user));
            if (!alreadyMember) {
                ChatGroupMember newMember = new ChatGroupMember();
                newMember.setChatGroup(chatGroup);
                newMember.setUser(user);
                chatGroup.getMembers().add(newMember);
            }
        }
        chatGroupRepository.save(chatGroup);
        return ResponseEntity.ok("Members added successfully");
    }

    public ResponseEntity<?> removeGroupChatMember(Long chatGroupId, List<String> memberIds) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        ChatGroup chatGroup = chatGroupRepository.findById(chatGroupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        boolean isAdmin = chatGroup.getMembers().stream()
                .anyMatch(member -> member.getUser().equals(currentUser) && member.getIsAdmin());
        if (!isAdmin) {
            throw new RuntimeException("You are not authorized to add members to this group");
        }

        for (String userId : memberIds) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            if (chatGroup.getMembers().stream().noneMatch(member -> member.getUser().getId().equals(user.getId()))) {
                return ResponseEntity.badRequest().body("User is not a member of this group");
            }
            chatGroup.getMembers().removeIf(member -> member.getUser().getId().equals(user.getId()));
        }

        chatGroupRepository.save(chatGroup);
        return ResponseEntity.ok("Members removed successfully");
    }

    public ResponseEntity<?> updateChatGroup(Long chatGroupId, UpdateChatGroupRequest updateChatGroupRequest, MultipartFile avatarImage) {
        return null;
    }

    public ResponseEntity<?> getChatGroups(int page, int size) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        List<ChatGroup> chatGroups = chatGroupRepository.findChatGroupsWithLatestMessages(currentUser.getId());
        System.out.println(chatGroups.get(0).getId());
        List<ChatGroupResponse> chatGroupResponses = chatGroups.stream()
                .map(chatGroup -> new ChatGroupResponse().toDto(chatGroup))
                .toList();

        return ResponseEntity.ok(chatGroupResponses);
    }
}
