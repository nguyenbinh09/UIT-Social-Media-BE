package com.example.demo.dtos.responses;

import com.example.demo.enums.NotificationType;
import com.example.demo.models.Notification;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationResponse {
    private Long id;
    private String message;
    private String actionUrl;
    private UserResponse sender;
    private GroupResponse group;
    private NotificationType type;
    private Boolean isRead;
    private LocalDateTime createdAt;

    public NotificationResponse toDto(Notification notification) {
        NotificationResponse notificationResponse = new NotificationResponse();
        notificationResponse.id = notification.getId();
        notificationResponse.message = notification.getMessage();
        notificationResponse.sender = new UserResponse().toDTO(notification.getSender());
        notificationResponse.actionUrl = notification.getActionUrl();
        if (notification.getGroup() != null) {
            notificationResponse.group = new GroupResponse().toDTO(notification.getGroup());
        }
        notificationResponse.type = notification.getType();
        notificationResponse.isRead = notification.getIsRead();
        notificationResponse.createdAt = notification.getCreatedAt();
        return notificationResponse;
    }

    public List<NotificationResponse> mapNotificationsToDTOs(List<Notification> notifications) {
        return notifications.stream()
                .map(this::toDto)
                .toList();
    }
}