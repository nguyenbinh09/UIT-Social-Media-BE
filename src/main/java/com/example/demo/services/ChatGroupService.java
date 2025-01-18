package com.example.demo.services;

import com.example.demo.dtos.requests.CreateChatGroupRequest;
import com.example.demo.dtos.requests.CreateGroupRequest;
import com.example.demo.dtos.requests.UpdateChatGroupRequest;
import com.example.demo.dtos.responses.ChatGroupMemberResponse;
import com.example.demo.dtos.responses.ChatGroupResponse;
import com.example.demo.enums.MediaType;
import com.example.demo.models.ChatGroup;
import com.example.demo.models.ChatGroupMember;
import com.example.demo.models.MediaFile;
import com.example.demo.models.User;
import com.example.demo.repositories.ChatGroupMemberRepository;
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
    private final MediaFileService mediaFileService;
    private final ChatGroupMemberRepository chatGroupMemberRepository;

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

        MediaFile avatar = new MediaFile();
        avatar.setUrl("https://firebasestorage.googleapis.com/v0/b/uit-social-network-f592d.appspot.com/o/chat-group-avatar-default.png?alt=media&token=9524cb5e-0ad2-4852-b593-17656eb050f6");
        avatar.setMediaType(MediaType.IMAGE);
        avatar.setFileName("chat-group-avatar-default.png");
        chatGroup.setAvatar(avatar);

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
            ChatGroupMember chatGroupMember = chatGroupMemberRepository.findByChatGroupIdAndUserId(chatGroupId, currentUser.getId())
                    .orElseThrow(() -> new RuntimeException("You are not a member of this group"));
            if (!chatGroupMember.getIsAdmin()) {
                throw new RuntimeException("You are not authorized to update this group");
            }
        }

        for (String userId : memberIds) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            boolean alreadyMember = chatGroup.getMembers().stream()
                    .anyMatch(member -> member.getUser().equals(user));
            if (!alreadyMember) {
                ChatGroupMember newMember = new ChatGroupMember();
                newMember.setChatGroup(chatGroup);
                newMember.setUser(user);
                chatGroup.getMembers().add(newMember);
            } else {
                return ResponseEntity.badRequest().body("User is already a member of this group");
            }
        }
        chatGroupRepository.save(chatGroup);
        return ResponseEntity.ok("Members added successfully");
    }

    @Transactional
    public ResponseEntity<?> removeGroupChatMember(Long chatGroupId, List<String> memberIds) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        ChatGroup chatGroup = chatGroupRepository.findById(chatGroupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        ChatGroupMember chatGroupMember = chatGroupMemberRepository.findByChatGroupIdAndUserId(chatGroupId, currentUser.getId())
                .orElseThrow(() -> new RuntimeException("You are not a member of this group"));
        if (!chatGroupMember.getIsAdmin()) {
            throw new RuntimeException("You are not authorized to remove member from this group");
        }

        for (String userId : memberIds) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            if (chatGroup.getMembers().stream().noneMatch(member -> member.getUser().getId().equals(user.getId()))) {
                return ResponseEntity.badRequest().body("User is not a member of this group");
            }
            if (user.getId().equals(currentUser.getId())) {
                return ResponseEntity.badRequest().body("You cannot remove yourself from the group");
            }
            chatGroup.getMembers().removeIf(member -> member.getUser().getId().equals(user.getId()));
        }

        chatGroupRepository.save(chatGroup);
        return ResponseEntity.ok("Members removed successfully");
    }

    @Transactional
    public ResponseEntity<?> updateChatGroup(Long chatGroupId, UpdateChatGroupRequest updateChatGroupRequest, MultipartFile avatarImage) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        ChatGroup chatGroup = chatGroupRepository.findById(chatGroupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));
        ChatGroupMember chatGroupMember = chatGroupMemberRepository.findByChatGroupIdAndUserId(chatGroupId, currentUser.getId())
                .orElseThrow(() -> new RuntimeException("You are not a member of this group"));
        if (!chatGroupMember.getIsAdmin()) {
            throw new RuntimeException("You are not authorized to update this group");
        }
        if (updateChatGroupRequest.getName() != null) {
            chatGroup.setName(updateChatGroupRequest.getName());
        }
        if (updateChatGroupRequest.getIsInvited() != null) {
            chatGroup.setIsInvited(updateChatGroupRequest.getIsInvited());
        }
        if (avatarImage != null) {
            MediaFile avatar = mediaFileService.uploadImage(avatarImage);
            chatGroup.setAvatar(avatar);
        }
        chatGroupRepository.save(chatGroup);
        return ResponseEntity.ok("Group updated successfully");
    }

    public ResponseEntity<?> getChatGroups(int page, int size) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        List<ChatGroup> chatGroups = chatGroupRepository.findChatGroupsWithLatestMessages(currentUser.getId());
        List<ChatGroupResponse> chatGroupResponses = chatGroups.stream()
                .map(chatGroup -> new ChatGroupResponse().toDto(chatGroup))
                .toList();

        return ResponseEntity.ok(chatGroupResponses);
    }


    public ResponseEntity<?> getMembers(Long chatGroupId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        ChatGroup chatGroup = chatGroupRepository.findById(chatGroupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        ChatGroupMember chatGroupMember = chatGroupMemberRepository.findByChatGroupIdAndUserId(chatGroupId, currentUser.getId())
                .orElseThrow(() -> new RuntimeException("You are not a member of this group"));
        if (!chatGroupMember.getIsAdmin()) {
            throw new RuntimeException("You are not authorized to update this group");
        }

        List<ChatGroupMember> members = chatGroup.getMembers();
        List<ChatGroupMemberResponse> memberResponses = members.stream()
                .map(member -> new ChatGroupMemberResponse().toDTO(member))
                .toList();
        return ResponseEntity.ok(memberResponses);
    }

    public ResponseEntity<?> getChatGroupById(Long chatGroupId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        ChatGroupMember chatGroupMember = chatGroupMemberRepository.findByChatGroupIdAndUserId(chatGroupId, currentUser.getId())
                .orElseThrow(() -> new RuntimeException("You are not a member of this group"));
        ChatGroup chatGroup = chatGroupRepository.findById(chatGroupId)
                .orElseThrow(() -> new IllegalArgumentException("Group not found"));

        ChatGroupResponse chatGroupResponse = new ChatGroupResponse().toDto(chatGroup);
        return ResponseEntity.ok(chatGroupResponse);
    }
}
