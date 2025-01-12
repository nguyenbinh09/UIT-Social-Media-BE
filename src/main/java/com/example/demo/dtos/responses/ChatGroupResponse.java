package com.example.demo.dtos.responses;


import com.example.demo.models.ChatGroup;
import com.example.demo.models.Message;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatGroupResponse {
    private Long id;
    private String groupName;
    private String avatarUrl;
    private String lastMessage;
    private String lastMessageSender;

    public ChatGroupResponse toDto(ChatGroup chatGroup) {
        this.setId(chatGroup.getId());
        this.setGroupName(chatGroup.getName());
        this.setAvatarUrl(chatGroup.getAvatar() != null ? chatGroup.getAvatar().getUrl() : null);
        if (!chatGroup.getMessages().isEmpty()) {
            Message lastMessage = chatGroup.getMessages().get(chatGroup.getMessages().size() - 1);
            this.setLastMessage(lastMessage.getContent());
            this.setLastMessageSender(lastMessage.getSender().getId());
        }
        return this;
    }
}