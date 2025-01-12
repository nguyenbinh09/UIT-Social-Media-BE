package com.example.demo.services;

import com.example.demo.dtos.responses.ContactResponse;
import com.example.demo.dtos.responses.InformationDetailResponse;
import com.example.demo.dtos.responses.ProfileResponse;
import com.example.demo.dtos.responses.SkillResponse;
import com.example.demo.models.Profile;
import com.example.demo.repositories.FollowRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ProfileResponseBuilder {
    private FollowRepository followRepository;

    public ProfileResponse toDTO(Profile profile) {
        ProfileResponse profileResponse = new ProfileResponse().toDTO(profile);

        if (profileResponse.getUserId() != null) {
            profileResponse.setFollowerCount(followRepository.countFollowersByUserId(profileResponse.getUserId()));
            profileResponse.setFollowingCount(followRepository.countFollowingByUserId(profileResponse.getUserId()));
        } else {
            profileResponse.setFollowerCount(0L);
            profileResponse.setFollowingCount(0L);
        }
        return profileResponse;
    }
}
