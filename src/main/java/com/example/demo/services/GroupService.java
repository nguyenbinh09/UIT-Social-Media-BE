package com.example.demo.services;

import com.example.demo.dtos.requests.CreateGroupRequest;
import com.example.demo.dtos.requests.UpdateGroupRequest;
import com.example.demo.dtos.responses.GroupMembershipRequestResponse;
import com.example.demo.dtos.responses.GroupResponse;
import com.example.demo.dtos.responses.UserResponse;
import com.example.demo.enums.InvitationStatus;
import com.example.demo.enums.MembershipRequestStatus;
import com.example.demo.enums.NotificationType;
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
import java.util.Map;
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
    private final ProfileResponseBuilder profileResponseBuilder;
    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;
    private final ProfileService profileService;
    private final FirebaseService firebaseService;

    @Transactional
    public ResponseEntity<?> createGroup(CreateGroupRequest createGroupRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        User currentUser = profileService.getUserWithProfile(user);
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
            for (User member : members) {
                GroupMembership newGroupMembership = new GroupMembership();
                newGroupMembership.setGroup(group);
                newGroupMembership.setUser(member);
                newGroupMembership.setRole(RoleName.MEMBER);
                group.getMembers().add(newGroupMembership);

                if (!member.getId().equals(currentUser.getId())) {
                    String title = currentUser.getUsername() + " created a new post in  " + group.getName() + " group";
                    String message = "You have been added to " + group.getName() + " group";
                    Profile profile = profileService.getProfileByUser(currentUser);
                    String avatar = profile.getProfileAvatar().getUrl();
                    String actionUrl = "/groups/" + group.getId();

                    Notification notification = new Notification();
                    notification.setSender(currentUser);
                    notification.setGroup(group);
                    notification.setReceiver(member);
                    notification.setType(NotificationType.GROUP_ADD_MEMBER);
                    notification.setMessage(message);
                    notification.setActionUrl(actionUrl);
                    notificationRepository.save(notification);
                    firebaseService.pushNotificationToUser(notification, member);

                    if (member.getFcmToken() != null) {
                        Map<String, String> dataPayload = Map.of(
                                "type", NotificationType.GROUP_ADD_MEMBER.name(),
                                "groupId", group.getId().toString(),
                                "actionUrl", actionUrl
                        );
                        notificationService.sendNotification(member, title, message, avatar, dataPayload);
                    }

                }
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
        User user = (User) authentication.getPrincipal();
        User currentUser = profileService.getUserWithProfile(user);
        Group group = groupRepository.findById(groupId).orElseThrow(() -> new RuntimeException("Group not found"));
        if (group.getIsDeleted().equals(true)) {
            return ResponseEntity.badRequest().body("Group is deleted");
        }
        Optional<GroupMembership> existingMember = groupMembershipRepository.findByUserIdAndGroupId(currentUser.getId(), group.getId());
        if (existingMember.isPresent()) {
            return ResponseEntity.badRequest().body("You are already a member of this group");
        }
        Optional<GroupMembershipRequest> existingRequest = groupMembershipRequestRepository.findByUserIdAndGroupId(currentUser.getId(), group.getId());
        if (existingRequest.isPresent() && existingRequest.get().getStatus().equals(MembershipRequestStatus.PENDING)) {
            return ResponseEntity.badRequest().body("Request already sent to join this group");
        }
        GroupMembershipRequest groupMembershipRequest = new GroupMembershipRequest();
        groupMembershipRequest.setUser(currentUser);
        groupMembershipRequest.setGroup(group);
        groupMembershipRequest.setStatus(MembershipRequestStatus.PENDING);
        groupMembershipRequestRepository.save(groupMembershipRequest);

        GroupMembership adminMembership = groupMembershipRepository.findAdminsByGroupId(group.getId(), RoleName.ADMIN).get(0);
        User admin = adminMembership.getUser();

        String title = currentUser.getUsername() + " requested to join group";
        String message = currentUser.getUsername() + " requested to join " + group.getName() + " group";
        Profile profile = profileService.getProfileByUser(currentUser);
        String avatar = profile.getProfileAvatar().getUrl();
        String actionUrl = "/group_requests/" + groupMembershipRequest.getId();

        Notification notification = new Notification();
        notification.setSender(currentUser);
        notification.setGroup(group);
        notification.setReceiver(admin);
        notification.setType(NotificationType.GROUP_MEMBERSHIP_REQUEST);
        notification.setMessage(message);
        notification.setActionUrl(actionUrl);
        notificationRepository.save(notification);
        firebaseService.pushNotificationToUser(notification, admin);
        if (admin.getFcmToken() != null) {
            Map<String, String> dataPayload = Map.of(
                    "type", NotificationType.GROUP_MEMBERSHIP_REQUEST.name(),
                    "requestId", groupMembershipRequest.getId().toString(),
                    "groupId", group.getId().toString(),
                    "actionUrl", actionUrl
            );
            notificationService.sendNotification(admin, title, message, avatar, dataPayload);
        }
        return ResponseEntity.ok("Successfully send request to join the group");
    }

    @Transactional
    public ResponseEntity<?> inviteUser(Long groupId, String userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        User currentUser = profileService.getUserWithProfile(user);
        Group group = groupRepository.findById(groupId).orElseThrow(() -> new RuntimeException("Group not found"));
        User invitee = profileService.getUserWithProfile(userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found")));
        groupMembershipRepository.findByUserIdAndGroupId(currentUser.getId(), group.getId())
                .orElseThrow(() -> new RuntimeException("You are not a member of this group"));
        Optional<GroupMembership> existingMember = groupMembershipRepository.findByUserIdAndGroupId(invitee.getId(), group.getId());
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

        String title = currentUser.getUsername() + " invited you to join group";
        String message = currentUser.getUsername() + " invited you to join " + group.getName() + " group";
        Profile profile = profileService.getProfileByUser(currentUser);
        String avatar = profile.getProfileAvatar().getUrl();
        String actionUrl = "/group_invitations/" + newInvitation.getId();

        Notification notification = new Notification();
        notification.setSender(currentUser);
        notification.setGroup(group);
        notification.setReceiver(invitee);
        notification.setType(NotificationType.GROUP_INVITATION);
        notification.setMessage(message);
        notification.setActionUrl(actionUrl);
        notificationRepository.save(notification);

        firebaseService.pushNotificationToUser(notification, invitee);
        if (invitee.getFcmToken() != null) {
            Map<String, String> dataPayload = Map.of(
                    "type", NotificationType.GROUP_INVITATION.name(),
                    "invitationId", newInvitation.getId().toString(),
                    "groupId", group.getId().toString(),
                    "actionUrl", actionUrl
            );
            notificationService.sendNotification(invitee, title, message, avatar, dataPayload);
        }

        return ResponseEntity.ok("Invitation sent successfully and send to " + invitee.getUsername());
    }

    @Transactional
    public ResponseEntity<?> respondToInvitation(Long invitationId, InvitationStatus status) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        User currentUser = profileService.getUserWithProfile(user);
        Invitation invitation = invitationRepository.findById(invitationId).orElseThrow(() -> new RuntimeException("Invitation not found"));
        User inviter = profileService.getUserWithProfile(invitation.getInviter());
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

            String title = currentUser.getUsername() + " accepted the invitation to join group";
            String message = currentUser.getUsername() + " accepted your invitation to join " + group.getName() + " group";
            Profile profile = profileService.getProfileByUser(currentUser);
            String avatar = profile.getProfileAvatar().getUrl();
            String actionUrl = "/groups/" + group.getId();

            Notification notification = new Notification();
            notification.setSender(currentUser);
            notification.setGroup(group);
            notification.setReceiver(inviter);
            notification.setType(NotificationType.POST);
            notification.setMessage(message);
            notification.setActionUrl(actionUrl);
            notificationRepository.save(notification);

            firebaseService.pushNotificationToUser(notification, inviter);
            if (inviter.getFcmToken() != null) {
                Map<String, String> dataPayload = Map.of(
                        "type", NotificationType.GROUP_INVITATION.name(),
                        "groupId", group.getId().toString(),
                        "actionUrl", actionUrl
                );
                notificationService.sendNotification(inviter, title, message, avatar, dataPayload);
            }
            return ResponseEntity.ok("Invitation accepted successfully");
        } else if (status.equals(InvitationStatus.REJECTED)) {
            invitation.setStatus(InvitationStatus.REJECTED);
            invitationRepository.save(invitation);

            String title = currentUser.getUsername() + " rejected the invitation to join group";
            String message = currentUser.getUsername() + " rejected your invitation to join " + group.getName() + " group";
            Profile profile = profileService.getProfileByUser(currentUser);
            String avatar = profile.getProfileAvatar().getUrl();
            String actionUrl = "";

            Notification notification = new Notification();
            notification.setSender(currentUser);
            notification.setGroup(group);
            notification.setReceiver(inviter);
            notification.setType(NotificationType.GROUP_INVITATION);
            notification.setMessage(message);
            notification.setActionUrl(actionUrl);
            notificationRepository.save(notification);

            firebaseService.pushNotificationToUser(notification, inviter);
            if (inviter.getFcmToken() != null) {
                Map<String, String> dataPayload = Map.of(
                        "type", NotificationType.GROUP_INVITATION.name(),
                        "groupId", group.getId().toString(),
                        "actionUrl", actionUrl
                );
                notificationService.sendNotification(inviter, title, message, avatar, dataPayload);
            }
            return ResponseEntity.ok("Invitation rejected successfully");
        } else {
            return ResponseEntity.badRequest().body("Invalid status");
        }
    }

    public ResponseEntity<?> addMember(Long groupId, String userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        User currentUser = profileService.getUserWithProfile(user);
        Group group = groupRepository.findById(groupId).orElseThrow(() -> new RuntimeException("Group not found"));
        User invitee = profileService.getUserWithProfile(userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found")));
        GroupMembership adminMembership = groupMembershipRepository.findByUserIdAndGroupId(currentUser.getId(), group.getId())
                .orElseThrow(() -> new RuntimeException("You are not a member of this group"));
        if (!adminMembership.getRole().equals(RoleName.ADMIN)) {
            return ResponseEntity.badRequest().body("You are not the admin of this group");
        }
        Optional<GroupMembership> existingMember = groupMembershipRepository.findByUserIdAndGroupId(invitee.getId(), group.getId());
        if (existingMember.isPresent()) {
            return ResponseEntity.badRequest().body("User is already a member of this group");
        }
        GroupMembership groupMembership = new GroupMembership();
        groupMembership.setGroup(group);
        groupMembership.setUser(invitee);
        groupMembership.setRole(RoleName.MEMBER);
        group.getMembers().add(groupMembership);
        groupMembershipRepository.save(groupMembership);

        String title = currentUser.getUsername() + " added you to group";
        String message = currentUser.getUsername() + " added you to " + group.getName() + " group";
        Profile profile = profileService.getProfileByUser(currentUser);
        String avatar = profile.getProfileAvatar().getUrl();
        String actionUrl = "/groups/" + group.getId();

        Notification notification = new Notification();
        notification.setSender(currentUser);
        notification.setGroup(group);
        notification.setReceiver(invitee);
        notification.setType(NotificationType.GROUP_ADD_MEMBER);
        notification.setMessage(message);
        notification.setActionUrl(actionUrl);
        notificationRepository.save(notification);

        firebaseService.pushNotificationToUser(notification, invitee);
        if (invitee.getFcmToken() != null) {
            Map<String, String> dataPayload = Map.of(
                    "type", NotificationType.GROUP_ADD_MEMBER.name(),
                    "groupId", group.getId().toString(),
                    "actionUrl", actionUrl
            );
            notificationService.sendNotification(invitee, title, message, avatar, dataPayload);
        }
        return ResponseEntity.ok("Member added successfully");
    }

    public ResponseEntity<?> handleRequest(Long requestId, MembershipRequestStatus status) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        User currentUser = profileService.getUserWithProfile(user);
        GroupMembershipRequest groupMembershipRequest = groupMembershipRequestRepository.findById(requestId).orElseThrow(() -> new RuntimeException("Request not found"));
        User requester = profileService.getUserWithProfile(groupMembershipRequest.getUser());
        Group group = groupRepository.findById(groupMembershipRequest.getGroup().getId()).orElseThrow(() -> new RuntimeException("Group not found"));
        if (group.getIsDeleted().equals(true)) {
            return ResponseEntity.badRequest().body("Group is deleted");
        }
        GroupMembership adminMembership = groupMembershipRepository.findByUserIdAndGroupId(currentUser.getId(), group.getId())
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
            groupMembership.setUser(requester);
            groupMembership.setRole(RoleName.MEMBER);
            group.getMembers().add(groupMembership);
            groupMembershipRepository.save(groupMembership);
            groupMembershipRequest.setStatus(MembershipRequestStatus.APPROVED);
            groupMembershipRequestRepository.save(groupMembershipRequest);

            String title = currentUser.getUsername() + " approved your request to join group";
            String message = currentUser.getUsername() + " approved your request to join " + group.getName() + " group";
            Profile profile = profileService.getProfileByUser(currentUser);
            String avatar = profile.getProfileAvatar().getUrl();
            String actionUrl = "/groups/" + group.getId();

            Notification notification = new Notification();
            notification.setSender(currentUser);
            notification.setGroup(group);
            notification.setReceiver(requester);
            notification.setType(NotificationType.GROUP_MEMBERSHIP_RESPONSE);
            notification.setMessage(message);
            notification.setActionUrl(actionUrl);
            notificationRepository.save(notification);

            firebaseService.pushNotificationToUser(notification, requester);
            if (requester.getFcmToken() != null) {
                Map<String, String> dataPayload = Map.of(
                        "type", NotificationType.GROUP_MEMBERSHIP_RESPONSE.name(),
                        "groupId", group.getId().toString(),
                        "actionUrl", actionUrl
                );
                notificationService.sendNotification(requester, title, message, avatar, dataPayload);
            }
        } else if (status.equals(MembershipRequestStatus.REJECTED)) {
            groupMembershipRequest.setStatus(MembershipRequestStatus.REJECTED);
            groupMembershipRequestRepository.save(groupMembershipRequest);
        } else {
            return ResponseEntity.badRequest().body("Invalid status");
        }
        return ResponseEntity.ok("Request responded " + status + " successfully");
    }

    public ResponseEntity<?> getMembers(Long groupId, int page, int size) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        Group group = groupRepository.findById(groupId).orElseThrow(() -> new RuntimeException("Group not found"));
        if (group.getIsDeleted().equals(true)) {
            return ResponseEntity.badRequest().body("Group is deleted");
        }
        GroupMembership groupMembership = groupMembershipRepository.findByUserIdAndGroupId(currentUser.getId(), group.getId())
                .orElseThrow(() -> new RuntimeException("You are not a member of this group"));
        List<GroupMembership> members = groupMembershipRepository.findAllByGroupId(group.getId());
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        List<User> users = userRepository.findAllUsersByIdIn(members.stream().map(member -> member.getUser().getId()).toList(), pageable).getContent();
        List<UserResponse> memberList = new UserResponse().mapUsersToDTOs(users, profileResponseBuilder);
        return ResponseEntity.ok(memberList);
    }

    public ResponseEntity<?> leaveGroup(Long groupId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        Group group = groupRepository.findById(groupId).orElseThrow(() -> new RuntimeException("Group not found"));
        if (group.getIsDeleted().equals(true)) {
            return ResponseEntity.badRequest().body("Group is deleted");
        }
        GroupMembership groupMembership = groupMembershipRepository.findByUserIdAndGroupId(currentUser.getId(), group.getId())
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

    public ResponseEntity<?> getRequests(Long groupId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        Group group = groupRepository.findById(groupId).orElseThrow(() -> new RuntimeException("Group not found"));
        if (group.getIsDeleted().equals(true)) {
            return ResponseEntity.badRequest().body("Group is deleted");
        }
        GroupMembership groupMembership = groupMembershipRepository.findByUserIdAndGroupId(currentUser.getId(), group.getId())
                .orElseThrow(() -> new RuntimeException("You are not a member of this group"));
        if (!groupMembership.getRole().equals(RoleName.ADMIN)) {
            return ResponseEntity.badRequest().body("You don't have permission to view requests");
        }
        List<GroupMembershipRequest> requests = groupMembershipRequestRepository.findRequestsByGroupId(group.getId());
        List<GroupMembershipRequestResponse> requestList = requests.stream().map(request -> new GroupMembershipRequestResponse().toDTO(request, profileResponseBuilder)).toList();
        return ResponseEntity.ok(requestList);
    }

    public ResponseEntity<?> getInvitations() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        List<Invitation> invitations = invitationRepository.findByInviteeId(currentUser.getId());
        List<GroupResponse> groupList = invitations.stream().map(invitation -> new GroupResponse().toDTO(invitation.getGroup())).toList();
        return ResponseEntity.ok(groupList);
    }
}
