package com.example.demo.services;

import com.example.demo.dtos.requests.SendGroupMessageRequest;
import com.example.demo.dtos.requests.SendMessageRequest;
import com.example.demo.dtos.responses.ConversationResponse;
import com.example.demo.dtos.responses.MessageResponse;
import com.example.demo.enums.FeedItemType;
import com.example.demo.enums.NotificationType;
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
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
@AllArgsConstructor
public class MessageService {
    private final UserRepository userRepository;
    private final PersonalConversationRepository personalConversationRepository;
    private final MessageRepository messageRepository;
    private final FirebaseService firebaseService;
    private final MediaFileService mediaFileService;
    private final FollowService followService;
    private final ChatGroupRepository chatGroupRepository;
    private final NotificationService notificationService;
    private final ProfileService profileService;
    private final ProfileResponseBuilder profileResponseBuilder;
    private final AdminRepository adminRepository;

    @Transactional
    public ResponseEntity<?> sendOneToOneMessage(SendMessageRequest sendMessageRequest, List<MultipartFile> mediaFiles) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        User sender = profileService.getUserWithProfile(currentUser);
        User receiver = profileService.getUserWithProfile(userRepository.findById(sendMessageRequest.getReceiverId())
                .orElseThrow(() -> new RuntimeException("Receiver not found")));

        if (sender.getId().equals(receiver.getId())) {
            return ResponseEntity.badRequest().body("You cannot send message to yourself");
        }

        Boolean isSenderFollowingReceiver = followService.isFollowing(sender.getId(), receiver.getId());
        Boolean isReceiverFollowingSender = followService.isFollowing(receiver.getId(), sender.getId());

        boolean isFollowing = isSenderFollowingReceiver && isReceiverFollowingSender;
        PersonalConversation conversation = personalConversationRepository
                .findByUserIds(sender.getId(), receiver.getId())
                .orElseGet(() -> createConversation(sender, receiver, !isFollowing));

        if (conversation.getIsPending() && messageRepository.existsByConversationId(conversation.getId())) {
            return ResponseEntity.badRequest().body("Conversation is pending. Wait for the receiver to approve.");
        }

        Message message = new Message();
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setContent(sendMessageRequest.getContent());
        message.setConversation(conversation);

        conversation.getMessages().add(message);
        Message savedMessage = messageRepository.save(message);

        if (mediaFiles != null && !mediaFiles.isEmpty()) {
            savedMessage.setMediaFiles(mediaFileService.uploadMediaFile(savedMessage.getId(), FeedItemType.MESSAGE, mediaFiles));
        }

        firebaseService.pushMessageToReceiver(savedMessage);

        if (receiver.getFcmToken() != null) {
            String title = "New message from " + sender.getUsername();
            String messageNotify = "You have a new message from " + sender.getUsername() + ": " + sendMessageRequest.getContent();
            Profile profile = profileService.getProfileByUser(sender);
            String avatar = profile.getProfileAvatar().getUrl();
            String actionUrl = "/conversations/" + conversation.getId();

            Map<String, String> dataPayload = Map.of(
                    "type", NotificationType.MESSAGE.name(),
                    "conversationId", conversation.getId().toString(),
                    "actionUrl", actionUrl
            );
            notificationService.sendNotification(receiver, title, messageNotify, avatar, dataPayload);
        }

        return ResponseEntity.ok().body("Message sent successfully");
    }

    private PersonalConversation createConversation(User user1, User user2, Boolean isPending) {
        PersonalConversation conversation = new PersonalConversation();
        conversation.setUser1(user1);
        conversation.setUser2(user2);
        conversation.setIsPending(isPending);
        return personalConversationRepository.save(conversation);
    }

    @Transactional
    public ResponseEntity<?> approvePendingMessage(Long conversationId, Boolean approve) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        PersonalConversation conversation = personalConversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));
        if (!conversation.getUser2().getId().equals(currentUser.getId())) {
            throw new RuntimeException("You are not authorized to approve this conversation");
        }
        if (approve) {
            conversation.setIsPending(false);
            personalConversationRepository.save(conversation);
            firebaseService.pushApprovedMessage(conversation.getMessages().get(0));
            return ResponseEntity.ok("Conversation approved successfully");
        } else {
            conversation.setIsDeleted(true);
            personalConversationRepository.save(conversation);
            firebaseService.deletePendingMessages(conversation.getMessages().get(0));
            return ResponseEntity.ok("Conversation rejected successfully");
        }
    }


    public ResponseEntity<?> markMessageAsRead(Long messageId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        Message message = messageRepository.findById(messageId).orElseThrow(() -> new RuntimeException("Message not found"));
        if (!message.getReceiver().getId().equals(currentUser.getId())) {
            throw new RuntimeException("You are not the receiver of this message");
        }
        message.setIsRead(true);
        messageRepository.save(message);
        return ResponseEntity.ok().body("Message marked as read");
    }

    @Transactional
    public ResponseEntity<?> sendGroupMessage(SendGroupMessageRequest request, List<MultipartFile> mediaFiles) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        User sender = profileService.getUserWithProfile(currentUser);

        ChatGroup chatGroup = chatGroupRepository.findById(request.getChatGroupId())
                .orElseThrow(() -> new RuntimeException("Group not found"));

        boolean isMember = chatGroup.getMembers().stream()
                .anyMatch(member -> member.getUser().getId().equals(sender.getId()));
        if (!isMember) {
            return ResponseEntity.badRequest().body("You are not a member of this group");
        }

        Message message = new Message();
        message.setSender(sender);
        message.setChatGroup(chatGroup);
        message.setContent(request.getMessageContent());

        chatGroup.getMessages().add(message);
        Message savedMessage = messageRepository.save(message);

        if (mediaFiles != null && !mediaFiles.isEmpty()) {
            savedMessage.setMediaFiles(mediaFileService.uploadMediaFile(savedMessage.getId(), FeedItemType.MESSAGE, mediaFiles));
        }

        firebaseService.pushGroupMessageToMembers(savedMessage);

        List<User> members = chatGroup.getMembers().stream()
                .map(ChatGroupMember::getUser)
                .filter(user -> !user.getId().equals(sender.getId()))
                .toList();

        CompletableFuture.runAsync(() -> notificationService.sendFCMNotificationToGroupMembers(savedMessage, members));
        return ResponseEntity.ok("Message sent successfully");
    }

    public ResponseEntity<?> getGroupMessages(Long groupId, int page, int size) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        ChatGroup chatGroup = chatGroupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        boolean isMember = chatGroup.getMembers().stream()
                .anyMatch(member -> member.getUser().getId().equals(currentUser.getId()));
        if (!isMember) {
            return ResponseEntity.badRequest().body("You are not a member of this group");
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        List<Message> messages = messageRepository.findByChatGroup(chatGroup, pageable);
        List<MessageResponse> messageResponses = messages.stream()
                .map(message -> new MessageResponse().toDTO(message, profileResponseBuilder))
                .toList();
        return ResponseEntity.ok(messageResponses);
    }

    public ResponseEntity<?> getConversationMessages(Long conversationId, int page, int size) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        PersonalConversation conversation = personalConversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));

        if (!conversation.getUser1().getId().equals(currentUser.getId()) && !conversation.getUser2().getId().equals(currentUser.getId())) {
            return ResponseEntity.badRequest().body("You are not a part of this conversation");
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        List<Message> messages = messageRepository.findByConversationId(conversationId, pageable);
        List<MessageResponse> messageResponses = messages.stream()
                .map(message -> new MessageResponse().toDTO(message, profileResponseBuilder))
                .toList();
        return ResponseEntity.ok(messageResponses);
    }

    public ResponseEntity<?> getPendingConversations() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        List<PersonalConversation> conversations = personalConversationRepository.findPendingConversationsWithLatestMessages(currentUser.getId());
        List<ConversationResponse> conversationResponses = conversations.stream()
                .map(conversation -> new ConversationResponse().toDto(conversation, currentUser.getId(), profileResponseBuilder))
                .toList();
        return ResponseEntity.ok(conversationResponses);
    }

    public ResponseEntity<?> getConversations(int page, int size) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        List<PersonalConversation> conversations = personalConversationRepository.findConversationsWithLatestMessages(currentUser.getId());
        List<ConversationResponse> conversationResponses = conversations.stream()
                .map(conversation -> new ConversationResponse().toDto(conversation, currentUser.getId(), profileResponseBuilder))
                .toList();
        return ResponseEntity.ok(conversationResponses);
    }

    public ResponseEntity<?> getConversationByUserId(String userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        PersonalConversation conversations = personalConversationRepository.findByUserIds(currentUser.getId(), user.getId()).orElseThrow(() -> new RuntimeException("Conversation not found"));
        ConversationResponse conversationResponse = new ConversationResponse().toDto(conversations, currentUser.getId(), profileResponseBuilder);
        return ResponseEntity.ok(conversationResponse);
    }

//    public ResponseEntity<?> sendMessageToAdmin(SendMessageRequest sendMessageRequest, List<MultipartFile> mediaFiles) {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        User currentUser = (User) authentication.getPrincipal();
//
//        if (currentUser.getStudent() == null) {
//            return ResponseEntity.badRequest().body("Only students can send messages to admin.");
//        }
//
//        Admin defaultAdmin = adminRepository.findFirstByOrderByIdAsc()
//                .orElseThrow(() -> new RuntimeException("No admin found in the system."));
//        User receiver = defaultAdmin.getUser();
//
//        PersonalConversation conversation = personalConversationRepository
//                .findByUserIds(currentUser.getId(), receiver.getId())
//                .orElseGet(() -> createConversation(currentUser, receiver, false));
//
//        Message message = new Message();
//        message.setSender(currentUser);
//        message.setReceiver(receiver);
//        message.setContent(sendMessageRequest.getContent());
//        message.setConversation(conversation);
//
//        Message savedMessage = messageRepository.save(message);
//
//        if (mediaFiles != null && !mediaFiles.isEmpty()) {
//            savedMessage.setMediaFiles(mediaFileService.uploadMediaFile(savedMessage.getId(), FeedItemType.MESSAGE, mediaFiles));
//        }
//
//        if (receiver.getFcmToken() != null) {
//            String title = "New message from " + currentUser.getUsername();
//            String messageNotify = "You have a new message from " + currentUser.getUsername() + ": " + sendMessageRequest.getContent();
//            Profile profile = profileService.getProfileByUser(currentUser);
//            String avatar = profile.getProfileAvatar().getUrl();
//
//            Map<String, String> dataPayload = Map.of(
//                    "type", NotificationType.MESSAGE.name(),
//                    "conversationId", conversation.getId().toString()
//            );
//            notificationService.sendNotification(receiver, title, messageNotify, avatar, dataPayload);
//        }
//
//        return ResponseEntity.ok().body("Message sent to the default admin successfully.");
//    }

}