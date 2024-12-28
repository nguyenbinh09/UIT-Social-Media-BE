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
    private MessageResponse lastMessage;

    public ChatGroupResponse toDto(ChatGroup chatGroup) {
        ChatGroupResponse chatGroupResponse = new ChatGroupResponse();
        chatGroupResponse.setId(chatGroup.getId());
        chatGroupResponse.setGroupName(chatGroup.getName());
        chatGroupResponse.setAvatarUrl(chatGroup.getAvatar() != null ? chatGroup.getAvatar().getUrl() : null);
        chatGroupResponse.setLastMessage(new MessageResponse().toDTO(chatGroup.getMessages().get(chatGroup.getMessages().size() - 1)));
        return chatGroupResponse;
    }
}