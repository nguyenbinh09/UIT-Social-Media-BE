package com.example.demo.dtos.responses;

import com.example.demo.enums.FollowRequestStatus;
import com.example.demo.models.FollowRequest;
import com.example.demo.services.ProfileResponseBuilder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FollowRequestResponse {
    private Long id;
    private UserResponse follower;
    private FollowRequestStatus status;

    public FollowRequestResponse toDTO(FollowRequest followRequest, ProfileResponseBuilder profileResponseBuilder) {
        this.setId(followRequest.getId());
        this.setFollower(new UserResponse().toDTO(followRequest.getFollower(), profileResponseBuilder));
        this.setStatus(followRequest.getStatus());
        return this;
    }
}
