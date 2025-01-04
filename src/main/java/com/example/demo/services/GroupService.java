package com.example.demo.services;

import com.example.demo.dtos.requests.CreateGroupRequest;
import com.example.demo.dtos.requests.UpdateGroupRequest;
import com.example.demo.dtos.responses.GroupResponse;
import com.example.demo.dtos.responses.UserResponse;
import com.example.demo.enums.InvitationStatus;
import com.example.demo.enums.MembershipRequestStatus;
import com.example.demo.enums.RoleName;
import com.example.demo.models.*;
import com.example.demo.repositories.*;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class GroupService {
    private final GroupRepository groupRepository;
    private final GroupMembershipRepository groupMembershipRepository;
    private final PrivacyRepository privacyRepository;
    private final UserRepository userRepository;
    private final InvitationRepository invitationRepository;
    private final GroupMembershipRequestRepository groupMembershipRequestRepository;

    @Transactional
    public ResponseEntity<?> createGroup(CreateGroupRequest createGroupRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        Privacy privacy = privacyRepository.findById(createGroupRequest.getPrivacyId()).orElseThrow(() -> new RuntimeException("Privacy not found"));

        Group group = createGroupRequest.toGroup();
        group.setPrivacy(privacy);
        group.setCreator(currentUser);
        GroupMembership groupMembership = new GroupMembership();
        groupMembership.setGroup(group);
        groupMembership.setUser(currentUser);
        groupMembership.setRole(RoleName.ADMIN);
        group.getMembers().add(groupMembership);
        if (!createGroupRequest.getMembers().isEmpty()) {
            List<User> members = userRepository.findAllByIdIn(createGroupRequest.getMembers());
            for (User user : members) {
                GroupMembership newGroupMembership = new GroupMembership();
                newGroupMembership.setGroup(group);
                newGroupMembership.setUser(user);
                newGroupMembership.setRole(RoleName.MEMBER);
                group.getMembers().add(newGroupMembership);
            }
        }
        groupRepository.save(group);
        return ResponseEntity.ok("Group created successfully");
    }

    @Transactional
    public ResponseEntity<?> updateGroup(Long groupId, UpdateGroupRequest updateGroupRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        Group group = groupRepository.findById(groupId).orElseThrow(() -> new RuntimeException("Group not found"));
        if (!group.getCreator().getId().equals(currentUser.getId())) {
            return ResponseEntity.badRequest().body("You are not the creator of this group");
        }
        if (updateGroupRequest.getName() != null) {
            group.setName(updateGroupRequest.getName());
        }
        if (updateGroupRequest.getDescription() != null) {
            group.setDescription(updateGroupRequest.getDescription());
        }
        if (updateGroupRequest.getPrivacyId() != null && !updateGroupRequest.getPrivacyId().equals(group.getPrivacy().getId())) {
            Privacy privacy = privacyRepository.findById(updateGroupRequest.getPrivacyId()).orElseThrow(() -> new RuntimeException("Privacy not found"));
            group.setPrivacy(privacy);
        }
        groupRepository.save(group);
        return ResponseEntity.ok("Group updated successfully");
    }

    public ResponseEntity<?> joinGroup(Long groupId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        Group group = groupRepository.findById(groupId).orElseThrow(() -> new RuntimeException("Group not found"));
        if (group.getIsDeleted().equals(true)) {
            return ResponseEntity.badRequest().body("Group is deleted");
        }
        Optional<GroupMembership> existingMember = groupMembershipRepository.findByUserIdAndGroupIdAndIsDeleted(currentUser.getId(), group.getId(), false);
        if (existingMember.isPresent()) {
            return ResponseEntity.badRequest().body("You are already a member of this group");
        }
        Optional<GroupMembershipRequest> existingRequest = groupMembershipRequestRepository.findByUserIdAndGroupIdAndIsDeleted(currentUser.getId(), group.getId(), false);
        if (existingRequest.isPresent() && existingRequest.get().getStatus().equals(MembershipRequestStatus.PENDING)) {
            return ResponseEntity.badRequest().body("Request already sent to join this group");
        }
        GroupMembershipRequest groupMembershipRequest = new GroupMembershipRequest();
        groupMembershipRequest.setUser(currentUser);
        groupMembershipRequest.setGroup(group);
        groupMembershipRequest.setStatus(MembershipRequestStatus.PENDING);
        groupMembershipRequestRepository.save(groupMembershipRequest);
        return ResponseEntity.ok("Successfully send request to join the group");
    }

    @Transactional
    public ResponseEntity<?> inviteUser(Long groupId, String userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        Group group = groupRepository.findById(groupId).orElseThrow(() -> new RuntimeException("Group not found"));
        User invitee = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("Invitee not found"));
        groupMembershipRepository.findByUserIdAndGroupIdAndIsDeleted(currentUser.getId(), group.getId(), false)
                .orElseThrow(() -> new RuntimeException("You are not a member of this group"));
        Optional<GroupMembership> existingMember = groupMembershipRepository.findByUserIdAndGroupIdAndIsDeleted(invitee.getId(), group.getId(), false);
        if (existingMember.isPresent()) {
            return ResponseEntity.badRequest().body("User is already a member of this group");
        }
        Optional<Invitation> existingInvitation = invitationRepository.findByGroupIdAndInviteeId(group.getId(), invitee.getId());
        if (existingInvitation.isPresent() && existingInvitation.get().getStatus().equals(InvitationStatus.PENDING)) {
            return ResponseEntity.badRequest().body("Invitation already sent to this user");
        }
        Invitation newInvitation = new Invitation();
        newInvitation.setInviter(currentUser);
        newInvitation.setGroup(group);
        newInvitation.setInvitee(invitee);
        newInvitation.setStatus(InvitationStatus.PENDING);
        invitationRepository.save(newInvitation);

        return ResponseEntity.ok("Invitation sent successfully and send to " + invitee.getUsername());
    }

    @Transactional
    public ResponseEntity<?> respondToInvitation(Long invitationId, InvitationStatus status) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        Invitation invitation = invitationRepository.findById(invitationId).orElseThrow(() -> new RuntimeException("Invitation not found"));
        Group group = groupRepository.findById(invitation.getGroup().getId()).orElseThrow(() -> new RuntimeException("Group not found"));
        if (group.getIsDeleted().equals(true)) {
            return ResponseEntity.badRequest().body("Group is deleted");
        }
        if (!invitation.getInvitee().getId().equals(currentUser.getId())) {
            return ResponseEntity.badRequest().body("You are not the invitee of this invitation");
        }
        if (!invitation.getStatus().equals(InvitationStatus.PENDING)) {
            return ResponseEntity.badRequest().body("Invitation is already responded");
        }
        if (status.equals(InvitationStatus.ACCEPTED)) {
            GroupMembership groupMembership = new GroupMembership();
            groupMembership.setGroup(invitation.getGroup());
            groupMembership.setUser(currentUser);
            groupMembership.setRole(RoleName.MEMBER);
            group.getMembers().add(groupMembership);
            groupMembershipRepository.save(groupMembership);
            invitation.setStatus(InvitationStatus.ACCEPTED);
            invitationRepository.save(invitation);
            return ResponseEntity.ok("Invitation accepted successfully");
        } else if (status.equals(InvitationStatus.REJECTED)) {
            invitation.setStatus(InvitationStatus.REJECTED);
            invitationRepository.save(invitation);
            return ResponseEntity.ok("Invitation rejected successfully");
        } else {
            return ResponseEntity.badRequest().body("Invalid status");
        }
    }

    public ResponseEntity<?> addMember(Long groupId, String userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        Group group = groupRepository.findById(groupId).orElseThrow(() -> new RuntimeException("Group not found"));
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        GroupMembership adminMembership = groupMembershipRepository.findByUserIdAndGroupIdAndIsDeleted(currentUser.getId(), group.getId(), false)
                .orElseThrow(() -> new RuntimeException("You are not a member of this group"));
        if (!adminMembership.getRole().equals(RoleName.ADMIN)) {
            return ResponseEntity.badRequest().body("You are not the admin of this group");
        }
        Optional<GroupMembership> existingMember = groupMembershipRepository.findByUserIdAndGroupIdAndIsDeleted(user.getId(), group.getId(), false);
        if (existingMember.isPresent()) {
            return ResponseEntity.badRequest().body("User is already a member of this group");
        }
        GroupMembership groupMembership = new GroupMembership();
        groupMembership.setGroup(group);
        groupMembership.setUser(user);
        groupMembership.setRole(RoleName.MEMBER);
        group.getMembers().add(groupMembership);
        groupMembershipRepository.save(groupMembership);
        return ResponseEntity.ok("Member added successfully");
    }

    public ResponseEntity<?> handleRequest(Long requestId, MembershipRequestStatus status) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        GroupMembershipRequest groupMembershipRequest = groupMembershipRequestRepository.findById(requestId).orElseThrow(() -> new RuntimeException("Request not found"));
        Group group = groupRepository.findById(groupMembershipRequest.getGroup().getId()).orElseThrow(() -> new RuntimeException("Group not found"));
        if (group.getIsDeleted().equals(true)) {
            return ResponseEntity.badRequest().body("Group is deleted");
        }
        GroupMembership adminMembership = groupMembershipRepository.findByUserIdAndGroupIdAndIsDeleted(currentUser.getId(), group.getId(), false)
                .orElseThrow(() -> new RuntimeException("You are not a member of this group"));
        if (!adminMembership.getRole().equals(RoleName.ADMIN)) {
            return ResponseEntity.badRequest().body("You don't have permission to handle this request");
        }
        if (!groupMembershipRequest.getStatus().equals(MembershipRequestStatus.PENDING)) {
            return ResponseEntity.badRequest().body("Request is already handled");
        }
        if (status.equals(MembershipRequestStatus.APPROVED)) {
            GroupMembership groupMembership = new GroupMembership();
            groupMembership.setGroup(group);
            groupMembership.setUser(groupMembershipRequest.getUser());
            groupMembership.setRole(RoleName.MEMBER);
            group.getMembers().add(groupMembership);
            groupMembershipRepository.save(groupMembership);
            groupMembershipRequest.setStatus(MembershipRequestStatus.APPROVED);
        } else if (status.equals(MembershipRequestStatus.REJECTED)) {
            groupMembershipRequest.setStatus(MembershipRequestStatus.REJECTED);
        } else {
            return ResponseEntity.badRequest().body("Invalid status");
        }
        groupMembershipRequestRepository.save(groupMembershipRequest);
        return ResponseEntity.ok("Request responded " + status + " successfully");
    }

    public ResponseEntity<?> getMembers(Long groupId, int page, int size) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        Group group = groupRepository.findById(groupId).orElseThrow(() -> new RuntimeException("Group not found"));
        if (group.getIsDeleted().equals(true)) {
            return ResponseEntity.badRequest().body("Group is deleted");
        }
        GroupMembership groupMembership = groupMembershipRepository.findByUserIdAndGroupIdAndIsDeleted(currentUser.getId(), group.getId(), false)
                .orElseThrow(() -> new RuntimeException("You are not a member of this group"));
        if (!groupMembership.getRole().equals(RoleName.ADMIN)) {
            return ResponseEntity.badRequest().body("You don't have permission to view members of this group");
        }
        List<GroupMembership> members = groupMembershipRepository.findAllByGroupId(group.getId());
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        List<User> users = userRepository.findAllUsersByIdIn(members.stream().map(member -> member.getUser().getId()).toList(), pageable).getContent();
        List<UserResponse> memberList = new UserResponse().mapUsersToDTOs(users);
        return ResponseEntity.ok(memberList);
    }

    public ResponseEntity<?> leaveGroup(Long groupId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        Group group = groupRepository.findById(groupId).orElseThrow(() -> new RuntimeException("Group not found"));
        if (group.getIsDeleted().equals(true)) {
            return ResponseEntity.badRequest().body("Group is deleted");
        }
        GroupMembership groupMembership = groupMembershipRepository.findByUserIdAndGroupIdAndIsDeleted(currentUser.getId(), group.getId(), false)
                .orElseThrow(() -> new RuntimeException("You are not a member of this group"));
        if (groupMembership.getRole().equals(RoleName.ADMIN)) {
            return ResponseEntity.badRequest().body("You can't leave the group because you are the admin");
        }
        groupMembership.setIsDeleted(true);
        groupMembershipRepository.save(groupMembership);
        return ResponseEntity.ok("Successfully left the group");
    }

    public ResponseEntity<?> getGroups(String userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        List<GroupMembership> groupMemberships = groupMembershipRepository.findAllByUserId(user.getId());
        List<Group> groups = groupMemberships.stream().map(GroupMembership::getGroup).toList();
        List<GroupResponse> groupList = new GroupResponse().mapGroupsToDTOs(groups);
        return ResponseEntity.ok(groupList);
    }


    public ResponseEntity<?> getGroup(Long groupId) {
        Group group = groupRepository.findById(groupId).orElseThrow(() -> new RuntimeException("Group not found"));
        GroupResponse groupResponse = new GroupResponse().toDTO(group);
        return ResponseEntity.ok(groupResponse);
    }
}
