package com.example.demo.services;

import com.example.demo.dtos.requests.AccountStatusRequest;
import com.example.demo.dtos.responses.NotificationResponse;
import com.example.demo.enums.AccountStatus;
import com.example.demo.enums.NotificationType;
import com.example.demo.models.ChatGroup;
import com.example.demo.models.User;
import com.example.demo.repositories.NotificationRepository;
import com.example.demo.repositories.UserRepository;
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
    private final UserRepository userRepository;
    private final ProfileResponseBuilder profileResponseBuilder;
    private final EmailService emailService;

    public void sendNotification(User user, String title, String body, String avatar, Map<String, String> data) {
        String fcmToken = user.getFcmToken();
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
        } catch (FirebaseMessagingException e) {
            if (e.getMessagingErrorCode() == MessagingErrorCode.UNREGISTERED
                    || e.getMessagingErrorCode() == MessagingErrorCode.INVALID_ARGUMENT) {
                System.err.println("Invalid FCM token, removing token: " + fcmToken);
                user.setFcmToken(null);
                userRepository.save(user);
            } else {
                throw new RuntimeException("Failed to send notification: " + e.getMessage(), e);
            }
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
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        com.example.demo.models.Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        if (!currentUser.getId().equals(notification.getReceiver().getId())) {
            return ResponseEntity.badRequest().body("You are not authorized to perform this action");
        }
        notification.setIsRead(true);
        notificationRepository.save(notification);
        return ResponseEntity.ok("Notification marked as read");
    }

    public ResponseEntity<?> getNotifications(int page, int size) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        List<com.example.demo.models.Notification> notifications = notificationRepository.findByReceiver(currentUser, pageable);

        List<NotificationResponse> notificationResponses = new NotificationResponse().mapNotificationsToDTOs(notifications, profileResponseBuilder);
        return ResponseEntity.ok(notificationResponses);
    }

    public void sendAccountStatusNotification(User user, AccountStatusRequest newStatus) {
        if (newStatus.getAccountStatus() == AccountStatus.ACTIVE) {
            String title = "Account activated";
            String body = "Your account has been activated. You can now access all features.";
            emailService.sendEmail(user.getEmail(), title, body);
        } else if (newStatus.getAccountStatus() == AccountStatus.BANNED) {
            String title = "Account banned";
            String body = "Your account has been banned.\n" +
                    "Reason: " + newStatus.getReason() +
                    "\nPlease contact support for more information.";
            emailService.sendEmail(user.getEmail(), title, body);
        }
    }
}
