package com.example.demo.dtos.responses;

import com.example.demo.enums.GenderType;
import com.example.demo.models.MediaFile;
import com.example.demo.models.Profile;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProfileResponse {
    private Long id;
    private String studentCode;
    private String nickName;
    private String tagName;
    private GenderType gender;
    private String bio;
    private String avatarUrl;
    private String userId;
    private String coverImageUrl;
    private InformationDetailResponse informationDetail;
    private Boolean isPrivate;
    private ContactResponse contact;
    private List<SkillResponse> skills;

    public ProfileResponse toDTO(Profile profile) {
        this.setId(profile.getId());
        this.setStudentCode(profile.getStudentCode());
        this.setNickName(profile.getNickName());
        this.setTagName(profile.getTagName());
        this.setGender(profile.getGender());
        this.setUserId(profile.getUser().getId());
        this.setBio(profile.getBio());
        this.setAvatarUrl(profile.getProfileAvatar().getUrl());
        if (profile.getProfileBackground() != null) {
            this.setCoverImageUrl(profile.getProfileBackground().getUrl());
        } else {
            this.setCoverImageUrl(null);
        }
        this.setInformationDetail(new InformationDetailResponse().toDto(profile.getInformationDetail()));
        this.setIsPrivate(profile.getIsPrivate());
        this.setContact(new ContactResponse().toDTO(profile.getContact()));
        this.setSkills(new SkillResponse().mapSkillsToDTOs(profile.getSkills()));
        return this;
    }
}
