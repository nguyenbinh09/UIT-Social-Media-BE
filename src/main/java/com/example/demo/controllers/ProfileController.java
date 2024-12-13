package com.example.demo.controllers;

import com.example.demo.dtos.requests.*;
import com.example.demo.enums.ProfileImageType;
import com.example.demo.services.ProfileService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.AllArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/profile")
@AllArgsConstructor
@PreAuthorize("hasRole('USER')")
@SecurityRequirement(name = "bearerAuth")
public class ProfileController {
    private final ProfileService profileService;

    @PostMapping("/create-profile")
    public ResponseEntity<?> createProfile(@RequestBody CreateProfileRequest createProfileRequest) {
        try {
            return profileService.createProfile(createProfileRequest);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/get-profile")
    public ResponseEntity<?> getProfile() {
        try {
            return profileService.getProfile();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping(value = "/update-profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateProfileImage(@RequestParam ProfileImageType profileImageType, @RequestPart MultipartFile profileImage) {
        try {
            return profileService.updateProfileImage(profileImageType, profileImage);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/set-default-profile-image")
    public ResponseEntity<?> setDefaultProfileImage(@RequestParam ProfileImageType profileImageType) {
        try {
            return profileService.setDefaultProfileImage(profileImageType);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/update-information-detail")
    public ResponseEntity<?> updateInformationDetail(@RequestBody UpdateInfoDetailRequest updateInfoDetailRequest) {
        try {
            return profileService.updateInformationDetail(updateInfoDetailRequest);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/update-contact")
    public ResponseEntity<?> updateContact(@RequestBody UpdateContactRequest updateContactRequest) {
        try {
            return profileService.updateContact(updateContactRequest);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/update-profile")
    public ResponseEntity<?> updateProfile(@RequestBody UpdateProfileRequest updateProfileRequest) {
        try {
            return profileService.updateProfile(updateProfileRequest);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/add-skill")
    public ResponseEntity<?> addSkill(@RequestBody AddSkillRequest addSkillRequest) {
        try {
            return profileService.addSkill(addSkillRequest);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
