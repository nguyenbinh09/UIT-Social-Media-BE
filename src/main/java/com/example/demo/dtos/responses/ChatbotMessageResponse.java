package com.example.demo.dtos.responses;

import com.example.demo.models.Message;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatbotMessageResponse {
    private String content;
    private Boolean isUser;
    private String createdAt;

    public ChatbotMessageResponse toDTO(Message message) {
        this.setContent(message.getContent());
        this.setIsUser(message.getSender() != null);
        this.setCreatedAt(message.getCreatedAt().toString());
        return this;
    }

    public List<ChatbotMessageResponse> mapsToDto(List<Message> messages) {
        return messages.stream().map(message -> new ChatbotMessageResponse().toDTO(message)).toList();
    }
}
