package com.example.demo.dtos.responses;

import com.example.demo.models.PersonalConversation;
import com.example.demo.models.User;
import com.example.demo.services.ProfileResponseBuilder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConversationResponse {
    private Long id;
    private UserResponse otherUser;
    private MessageResponse lastMessage;

    public ConversationResponse toDto(PersonalConversation conversation, String currentUserId, ProfileResponseBuilder profileResponseBuilder) {
        ConversationResponse conversationResponse = new ConversationResponse();
        conversationResponse.setId(conversation.getId());
        User otherUser = conversation.getUser1().getId().equals(currentUserId)
                ? conversation.getUser2()
                : conversation.getUser1();

        conversationResponse.setOtherUser(new UserResponse().toDTO(otherUser, profileResponseBuilder));

        if (!conversation.getMessages().isEmpty()) {
            conversationResponse.setLastMessage(new MessageResponse().toDTO(
                    conversation.getMessages().get(conversation.getMessages().size() - 1)
                    , profileResponseBuilder));
        }
        return conversationResponse;
    }
}
