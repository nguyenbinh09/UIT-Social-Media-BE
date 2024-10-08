package com.example.demo.dtos.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class FollowUserRequest {
    private String followedId;
    private String followerId;
}
