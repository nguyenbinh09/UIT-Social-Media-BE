package com.example.demo.services;

import com.example.demo.dtos.responses.NotificationResponse;
import com.example.demo.enums.NotificationType;
import com.example.demo.models.ChatGroup;
import com.example.demo.models.User;
import com.example.demo.repositories.NotificationRepository;
import com.google.firebase.messaging.*;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@AllArgsConstructor
public class NotificationService {
    private final NotificationRepository notificationRepository;

    public void sendNotification(String fcmToken, String title, String body, String avatar, Map<String, String> data) {
        try {
            Notification notification = Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .setImage(avatar)
                    .build();

            Message.Builder messageBuilder = Message.builder()
                    .setToken(fcmToken)
                    .setNotification(notification);

            if (data != null && !data.isEmpty()) {
                messageBuilder.putAllData(data);
            }

            Message message = messageBuilder.build();

            String response = FirebaseMessaging.getInstance().send(message);
            System.out.println("Successfully sent notification: " + response);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send notification: " + e.getMessage(), e);
        }
    }

    public void sendFCMNotificationToGroupMembers(com.example.demo.models.Message savedMessage, List<User> members) {
        List<String> fcmTokens = members.stream()
                .map(User::getFcmToken)
                .filter(Objects::nonNull)
                .toList();

        if (fcmTokens.isEmpty()) {
            System.out.println("No FCM tokens found for group members.");
            return;
        }

        try {
            ChatGroup chatGroup = savedMessage.getChatGroup();
            String title = "New message from " + savedMessage.getSender().getUsername();
            String body = "You have a new message from group" + chatGroup.getName() + ": " + savedMessage.getContent();
            String avatar = chatGroup.getAvatar().getUrl();
            String actionUrl = "/chat-groups/" + chatGroup.getId();

            Map<String, String> dataPayload = Map.of(
                    "type", NotificationType.MESSAGE.name(),
                    "chatGroupId", chatGroup.getId().toString(),
                    "actionUrl", actionUrl
            );

            Notification notification = Notification.builder()
                    .setTitle(title)
                    .setBody(body)
                    .setImage(avatar)
                    .build();

            MulticastMessage multicastMessage = MulticastMessage.builder()
                    .addAllTokens(fcmTokens)
                    .setNotification(notification)
                    .putAllData(dataPayload)
                    .build();
            
            BatchResponse response = FirebaseMessaging.getInstance().sendMulticast(multicastMessage);

            System.out.println("Successfully sent notifications to group members: " + response.getSuccessCount());
            if (response.getFailureCount() > 0) {
                System.out.println("Failed to send to some tokens: " + response.getFailureCount());
            }
        } catch (Exception e) {
            System.err.println("Error sending FCM notifications: " + e.getMessage());
        }
    }

    @Transactional
    public ResponseEntity<?> markAsRead(Long id) {
        com.example.demo.models.Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        notification.setIsRead(true);
        notificationRepository.save(notification);
        return ResponseEntity.ok("Notification marked as read");
    }

    public ResponseEntity<?> getNotifications(int page, int size) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        List<com.example.demo.models.Notification> notifications = notificationRepository.findByReceiver(currentUser, pageable);

        List<NotificationResponse> notificationResponses = new NotificationResponse().mapNotificationsToDTOs(notifications);
        return ResponseEntity.ok(notificationResponses);
    }
}
