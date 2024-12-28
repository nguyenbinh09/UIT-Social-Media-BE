package com.example.demo.services;

import com.example.demo.dtos.requests.*;
import com.example.demo.dtos.responses.ProfileResponse;
import com.example.demo.dtos.responses.SkillResponse;
import com.example.demo.enums.GenderType;
import com.example.demo.enums.MediaType;
import com.example.demo.enums.ProfileImageType;
import com.example.demo.models.*;
import com.example.demo.repositories.*;
import com.example.demo.utils.MediaFIleUtils;
import com.google.cloud.storage.Blob;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class ProfileService {
    private final ProfileRepository profileRepository;
    private final InformationDetailRepository informationDetailRepository;
    private final ContactRepository contactRepository;
    private final MediaFileRepository mediaFileRepository;
    private final FirebaseService firebaseService;
    private final UserRepository userRepository;

    @Transactional
    public ResponseEntity<?> createProfile(CreateProfileRequest createProfileRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        if (profileRepository.existsByTagName(createProfileRequest.getTagName())) {
            throw new RuntimeException("Tag name already exists. Please choose another.");
        }

        if (currentUser.getProfile() != null) {
            throw new RuntimeException("Profile already exists");
        }

        Profile profile = new Profile();
        InformationDetail informationDetail = new InformationDetail();
        Contact contact = new Contact();

        profile.setStudentCode(createProfileRequest.getCode());
        profile.setNickName(createProfileRequest.getNickName());
        profile.setTagName(createProfileRequest.getTagName());
        profile.setBirthDate(createProfileRequest.getBirthday());
        profile.setGender(createProfileRequest.getGender());
        if (profile.getGender() == GenderType.FEMALE) {
            MediaFile avatar = new MediaFile();
            avatar.setFileName("male-default-avatar.png");
            String maleDefaultAvatar = "https://firebasestorage.googleapis.com/v0/b/uit-social-network-f592d.appspot.com/o/male-default-avatar.png?alt=media&token=7e8f5970-35fa-4d3d-97ae-361dfb91903d";
            avatar.setUrl(maleDefaultAvatar);
            avatar.setMediaType(MediaType.IMAGE);
            profile.setProfileAvatar(avatar);
        } else if (profile.getGender() == GenderType.MALE) {
            MediaFile avatar = new MediaFile();
            avatar.setFileName("female-default-avatar.png");
            String femaleDefaultAvatar = "https://firebasestorage.googleapis.com/v0/b/uit-social-network-f592d.appspot.com/o/female-default-avatar.png?alt=media&token=248e1df8-df98-44ba-803b-56e620f1c762";
            avatar.setUrl(femaleDefaultAvatar);
            avatar.setMediaType(MediaType.IMAGE);
            profile.setProfileAvatar(avatar);
        }

        informationDetail.setMajor(createProfileRequest.getMajor());
        informationDetail.setSchoolYear(createProfileRequest.getSchoolYear());
        informationDetail.setActivityClass(createProfileRequest.getActivityClass());
        profile.setInformationDetail(informationDetail);

        contact.setEmailToContact(currentUser.getEmail());
        contact.setPhoneNumber(createProfileRequest.getPhoneNumber());
        contact.setAddress(createProfileRequest.getAddress());
        profile.setContact(contact);

        Profile savedProfile = profileRepository.save(profile);

        currentUser.setProfile(savedProfile);
        userRepository.save(currentUser);
        return ResponseEntity.ok("Profile created successfully");
    }

    public ResponseEntity<?> getProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        if (currentUser.getProfile() == null) {
            return null;
        }

        Profile profile = profileRepository.findById(currentUser.getProfile().getId()).orElseThrow(() -> new RuntimeException("Profile not found"));
        ProfileResponse profileResponse = new ProfileResponse().toDTO(profile);
        return ResponseEntity.ok(profileResponse);
    }


    @Transactional
    public ResponseEntity<?> updateProfileImage(ProfileImageType profileImageType, MultipartFile profileImage) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        MediaType mediaType = MediaFIleUtils.determineMediaType(profileImage);
        if (mediaType != MediaType.IMAGE) {
            throw new RuntimeException("Invalid media type. Please upload an image file");
        }
        Profile profile = profileRepository.findById(currentUser.getProfile().getId()).orElseThrow(() -> new RuntimeException("Profile not found"));
        MediaFile mediaFile = new MediaFile();
        if (profileImageType == ProfileImageType.AVATAR) {
            Blob avatarBlob = firebaseService.uploadFile(profileImage);
            mediaFile.setFileName(avatarBlob.getName());
            mediaFile.setUrl(avatarBlob.getMediaLink());
            mediaFile.setMediaType(mediaType);
            profile.setProfileAvatar(mediaFile);
        } else if (profileImageType == ProfileImageType.BACKGROUND) {
            Blob backgroundBlob = firebaseService.uploadFile(profileImage);
            mediaFile.setFileName(backgroundBlob.getName());
            mediaFile.setUrl(backgroundBlob.getMediaLink());
            mediaFile.setMediaType(mediaType);
            profile.setProfileBackground(mediaFile);
        }
        profileRepository.save(profile);
        return ResponseEntity.ok("Profile image updated successfully");
    }


//    public ResponseEntity<?> setDefaultProfileImage(ProfileImageType profileImageType) {
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        User currentUser = (User) authentication.getPrincipal();
//        Profile profile = profileRepository.findById(currentUser.getProfile().getId()).orElseThrow(() -> new RuntimeException("Profile not found"));
//
//        if (profileImageType == ProfileImageType.AVATAR) {
//            if (profile.getGender() == GenderType.FEMALE) {
//                MediaFile avatar = mediaFileRepository.findByFileName("female-default-avatar.png");
//                profile.setProfileAvatar(avatar);
//            } else if (profile.getGender() == GenderType.MALE) {
//                MediaFile avatar = mediaFileRepository.findByFileName("male-default-avatar.png");
//                profile.setProfileAvatar(avatar);
//            }
//        } else if (profileImageType == ProfileImageType.BACKGROUND) {
//            profile.setProfileBackground(null);
//        }
//        profileRepository.save(profile);
//        return ResponseEntity.ok("Default profile image set successfully");
//    }


    public ResponseEntity<?> updateInformationDetail(UpdateInfoDetailRequest updateInfoDetailRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        Profile profile = profileRepository.findById(currentUser.getProfile().getId()).orElseThrow(() -> new RuntimeException("Profile not found"));

        InformationDetail informationDetail = profile.getInformationDetail();
        if (updateInfoDetailRequest.getFullName() != null) {
            informationDetail.setFullName(updateInfoDetailRequest.getFullName());
        }
        if (updateInfoDetailRequest.getMajor() != null) {
            informationDetail.setMajor(updateInfoDetailRequest.getMajor());
        }
        if (updateInfoDetailRequest.getSchoolYear() != null) {
            informationDetail.setSchoolYear(updateInfoDetailRequest.getSchoolYear());
        }
        if (updateInfoDetailRequest.getActivityClass() != null) {
            informationDetail.setActivityClass(updateInfoDetailRequest.getActivityClass());
        }
        if (updateInfoDetailRequest.getWork() != null) {
            informationDetail.setWork(updateInfoDetailRequest.getWork());
        }
        if (updateInfoDetailRequest.getCurrentCity() != null) {
            informationDetail.setCurrentCity(updateInfoDetailRequest.getCurrentCity());
        }
        if (updateInfoDetailRequest.getHomeTown() != null) {
            informationDetail.setHomeTown(updateInfoDetailRequest.getHomeTown());
        }
        informationDetailRepository.save(informationDetail);
        return ResponseEntity.ok("Information detail updated successfully");
    }

    public ResponseEntity<?> updateContact(UpdateContactRequest updateContactRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        Profile profile = profileRepository.findById(currentUser.getProfile().getId()).orElseThrow(() -> new RuntimeException("Profile not found"));

        Contact contact = profile.getContact();
        if (updateContactRequest.getEmailToContact() != null) {
            contact.setEmailToContact(updateContactRequest.getEmailToContact());
        }
        if (updateContactRequest.getPhoneNumber() != null) {
            contact.setPhoneNumber(updateContactRequest.getPhoneNumber());
        }
        if (updateContactRequest.getAddress() != null) {
            contact.setAddress(updateContactRequest.getAddress());
        }
        contactRepository.save(contact);
        return ResponseEntity.ok("Contact updated successfully");
    }

    public ResponseEntity<?> updateProfile(UpdateProfileRequest updateProfileRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        Profile profile = profileRepository.findById(currentUser.getProfile().getId()).orElseThrow(() -> new RuntimeException("Profile not found"));

        if (updateProfileRequest.getStudentCode() != null) {
            profile.setStudentCode(updateProfileRequest.getStudentCode());
        }
        if (updateProfileRequest.getNickName() != null) {
            profile.setNickName(updateProfileRequest.getNickName());
        }
        if (updateProfileRequest.getTagName() != null) {
            profile.setTagName(updateProfileRequest.getTagName());
        }
        if (updateProfileRequest.getBirthDate() != null) {
            profile.setBirthDate(updateProfileRequest.getBirthDate());
        }
        if (updateProfileRequest.getGender() != null && updateProfileRequest.getGender() != profile.getGender()) {
            profile.setGender(updateProfileRequest.getGender());
            if (profile.getGender() == GenderType.FEMALE) {
                MediaFile avatar = mediaFileRepository.findByFileName("female-default-avatar.png");
                profile.setProfileAvatar(avatar);
            } else if (profile.getGender() == GenderType.MALE) {
                MediaFile avatar = mediaFileRepository.findByFileName("male-default-avatar.png");
                profile.setProfileAvatar(avatar);
            }
        }
        if (updateProfileRequest.getBio() != null) {
            profile.setBio(updateProfileRequest.getBio());
        }
        if (updateProfileRequest.getIsPrivate() != null && updateProfileRequest.getIsPrivate() != profile.getIsPrivate()) {
            profile.setIsPrivate(updateProfileRequest.getIsPrivate());
        }
        profileRepository.save(profile);
        return ResponseEntity.ok("Profile updated successfully");
    }

    public ResponseEntity<?> addSkill(AddSkillRequest addSkillRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        Profile profile = profileRepository.findById(currentUser.getProfile().getId()).orElseThrow(() -> new RuntimeException("Profile not found"));

        Skill skill = new Skill();
        skill.setName(addSkillRequest.getName());
        skill.setRate(addSkillRequest.getRate());
        skill.setDescription(addSkillRequest.getDescription());
        skill.setProfile(profile);
        profile.getSkills().add(skill);
        profileRepository.save(profile);
        return ResponseEntity.ok("Skill added successfully");
    }

    public ResponseEntity<?> getSkills() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        Profile profile = profileRepository.findById(currentUser.getProfile().getId()).orElseThrow(() -> new RuntimeException("Profile not found"));
        List<SkillResponse> skillResponses = new SkillResponse().mapSkillsToDTOs(profile.getSkills());
        return ResponseEntity.ok(skillResponses);
    }

    public ResponseEntity<?> getProfileById(Long id) {
        Optional<Profile> profile = profileRepository.findById(id);
        if (profile.isEmpty()) {
            throw new RuntimeException("Profile not found");
        }
        ProfileResponse profileResponse = new ProfileResponse().toDTO(profile.get());
        return ResponseEntity.ok(profileResponse);
    }

    public ResponseEntity<?> getProfileByUserId(String userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            throw new RuntimeException("User not found");
        }
        if (user.get().getProfile() == null) {
            throw new RuntimeException("Profile not found");
        }
        ProfileResponse profileResponse = new ProfileResponse().toDTO(user.get().getProfile());
        return ResponseEntity.ok(profileResponse);
    }
}
