package com.example.demo.dtos.responses;

import com.example.demo.models.ChatGroupMember;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatGroupMemberResponse {
    private Long id;
    private Long chatGroupId;
    private String userId;
    private String nickName;
    private String avatarUrl;
    private Boolean isAdmin;
    private Boolean isMuted;

    public ChatGroupMemberResponse toDTO(ChatGroupMember chatGroupMember) {
        this.setId(chatGroupMember.getId());
        this.setChatGroupId(chatGroupMember.getChatGroup().getId());
        this.setUserId(chatGroupMember.getUser().getId());
        if (chatGroupMember.getUser().getStudent() != null) {
            this.setNickName(chatGroupMember.getUser().getStudent().getProfile().getNickName());
            this.setAvatarUrl(chatGroupMember.getUser().getStudent().getProfile().getProfileAvatar().getUrl());
        } else {
            this.setNickName(chatGroupMember.getUser().getLecturer().getProfile().getNickName());
            this.setAvatarUrl(chatGroupMember.getUser().getLecturer().getProfile().getProfileAvatar().getUrl());
        }
        this.setIsAdmin(chatGroupMember.getIsAdmin());
        this.setIsMuted(chatGroupMember.getIsMuted());
        return this;
    }
}
