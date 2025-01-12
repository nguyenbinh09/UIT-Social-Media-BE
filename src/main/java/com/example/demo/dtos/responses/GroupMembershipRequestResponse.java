package com.example.demo.dtos.responses;

import com.example.demo.models.GroupMembershipRequest;
import com.example.demo.services.ProfileResponseBuilder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupMembershipRequestResponse {
    private Long id;
    private UserResponse user;
    private Long groupId;

    public GroupMembershipRequestResponse toDTO(GroupMembershipRequest groupMembershipRequest, ProfileResponseBuilder profileResponseBuilder) {
        this.id = groupMembershipRequest.getId();
        this.user = new UserResponse().toDTO(groupMembershipRequest.getUser(), profileResponseBuilder);
        this.groupId = groupMembershipRequest.getGroup().getId();
        return this;
    }
}
