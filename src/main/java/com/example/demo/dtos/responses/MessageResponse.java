package com.example.demo.dtos.responses;

import com.example.demo.models.MediaFile;
import com.example.demo.models.Message;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageResponse {
    private Long id;
    private String senderId;
    private String receiverId;
    private Long chatGroupId;
    private String content;
    private LocalDateTime createdAt;
    private List<String> mediaFiles;

    public MessageResponse toDTO(Message message) {
        this.setId(message.getId());
        this.setSenderId(message.getSender().getId());
        if (message.getReceiver() != null) {
            this.setReceiverId(message.getReceiver().getId());
        }
        if (message.getChatGroup() != null) {
            this.setChatGroupId(message.getChatGroup().getId());
        }
        this.setContent(message.getContent());
        this.setMediaFiles(message.getMediaFiles().stream().map(MediaFile::getUrl).toList());
        this.setCreatedAt(message.getCreatedAt());
        return this;
    }
}
