package com.example.demo.dtos.responses;

import com.example.demo.enums.FollowRequestStatus;
import com.example.demo.models.FollowRequest;
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

    public FollowRequestResponse toDTO(FollowRequest followRequest) {
        this.setId(followRequest.getId());
        this.setFollower(new UserResponse().toDTO(followRequest.getFollower()));
        this.setStatus(followRequest.getStatus());
        return this;
    }
}
