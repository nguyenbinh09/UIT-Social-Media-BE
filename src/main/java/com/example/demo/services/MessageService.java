package com.example.demo.services;

import com.example.demo.dtos.requests.SendMessageRequest;
import com.example.demo.enums.FeedItemType;
import com.example.demo.models.Message;
import com.example.demo.models.PersonalConversation;
import com.example.demo.models.User;
import com.example.demo.repositories.MessageRepository;
import com.example.demo.repositories.PersonalConversationRepository;
import com.example.demo.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
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

    @Transactional
    public ResponseEntity<?> sendOneToOneMessage(SendMessageRequest sendMessageRequest, List<MultipartFile> mediaFiles) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User sender = (User) authentication.getPrincipal();
        User receiver = userRepository.findById(sendMessageRequest.getReceiverId())
                .orElseThrow(() -> new RuntimeException("User not found"));

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

        return ResponseEntity.ok().body("Message sent successfully");
    }

    private PersonalConversation createConversation(User user1, User user2, Boolean isPending) {
        System.out.println(isPending);
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
            firebaseService.deletePendingMessages(conversationId);
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
}