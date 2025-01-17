package com.example.demo.dtos.responses;

import com.example.demo.models.Invitation;
import com.example.demo.services.ProfileResponseBuilder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InvitationResponse {
    private Long id;
    private GroupResponse group;
    private UserResponse inviter;

    public InvitationResponse toDTO(Invitation invitation, ProfileResponseBuilder profileResponseBuilder) {
        InvitationResponse invitationResponse = new InvitationResponse();
        invitationResponse.setId(invitation.getId());
        invitationResponse.setGroup(new GroupResponse().toDTO(invitation.getGroup()));
        invitationResponse.setInviter(new UserResponse().toDTO(invitation.getInviter(), profileResponseBuilder));
        return invitationResponse;
    }

    public List<InvitationResponse> mapInvitationsToDTOs(List<Invitation> invitations, ProfileResponseBuilder profileResponseBuilder) {
        return invitations.stream()
                .map(invitation -> toDTO(invitation, profileResponseBuilder))
                .toList();
    }
}
