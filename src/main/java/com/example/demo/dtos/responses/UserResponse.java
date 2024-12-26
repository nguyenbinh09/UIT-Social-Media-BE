package com.example.demo.dtos.responses;


import com.example.demo.models.User;
import com.example.demo.repositories.ProfileRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {
    private String id;
    private String username;
    private String email;
    private String nickname;
    private String tagName;
    private String avatarUrl;
    private List<String> roles;

    public UserResponse toDTO(User user) {
        this.setId(user.getId());
        this.setUsername(user.getUsername());
        this.setNickname(user.getProfile().getNickName());
        this.setTagName(user.getProfile().getTagName());
        this.setAvatarUrl(user.getProfile().getProfileAvatar().getUrl());
        this.setEmail(user.getEmail());
        this.setRoles(user.getRoles().stream().map(role -> role.getName().name()).toList());
        return this;
    }

    public List<UserResponse> mapUsersToDTOs(List<User> users) {
        return users.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
}
