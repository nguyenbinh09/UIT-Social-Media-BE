package com.example.demo.services;

import com.example.demo.dtos.requests.*;
import com.example.demo.dtos.responses.ProfileResponse;
import com.example.demo.dtos.responses.SkillResponse;
import com.example.demo.enums.GenderType;
import com.example.demo.enums.MediaType;
import com.example.demo.enums.ProfileImageType;
import com.example.demo.enums.RoleName;
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
    private final StudentRepository studentRepository;
    private final LecturerRepository lecturerRepository;
    private final ProfileResponseBuilder profileResponseBuilder;
    private final MediaFileService mediaFileService;

    @Transactional
    public ResponseEntity<?> createProfile(CreateProfileRequest createProfileRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        if (profileRepository.existsByTagName(createProfileRequest.getTagName())) {
            throw new RuntimeException("Tag name already exists. Please choose another.");
        }

        if (currentUser.getStudent() != null || currentUser.getLecturer() != null) {
            throw new RuntimeException("Profile already exists");
        }

        Profile profile = new Profile();
        profile.setNickName(createProfileRequest.getNickName());
        profile.setTagName(createProfileRequest.getTagName());
        profile.setBirthDate(createProfileRequest.getBirthday());
        profile.setGender(createProfileRequest.getGender());

        MediaFile avatar = new MediaFile();
        if (profile.getGender() == GenderType.FEMALE) {
            avatar.setFileName("female-default-avatar.png");
            avatar.setUrl("https://firebasestorage.googleapis.com/v0/b/uit-social-network-f592d.appspot.com/o/female-default-avatar.png?alt=media&token=248e1df8-df98-44ba-803b-56e620f1c762");
        } else {
            avatar.setFileName("male-default-avatar.png");
            avatar.setUrl("https://firebasestorage.googleapis.com/v0/b/uit-social-network-f592d.appspot.com/o/male-default-avatar.png?alt=media&token=7e8f5970-35fa-4d3d-97ae-361dfb91903d");
        }
        avatar.setMediaType(MediaType.IMAGE);
        profile.setProfileAvatar(avatar);

        InformationDetail informationDetail = new InformationDetail();
        profile.setInformationDetail(informationDetail);

        Contact contact = new Contact();
        contact.setEmailToContact(currentUser.getEmail());
        contact.setPhoneNumber(createProfileRequest.getPhoneNumber());
        contact.setAddress(createProfileRequest.getAddress());
        profile.setContact(contact);

        if (currentUser.getRole().getName().equals(RoleName.STUDENT)) {
            Student student = new Student();
            student.setUser(currentUser);
            student.setStudentCode(createProfileRequest.getCode());
            student.setMajor(createProfileRequest.getStudent().getMajor());
            student.setClassName(createProfileRequest.getStudent().getClassName());
            student.setYearOfAdmission(createProfileRequest.getStudent().getYearOfAdmission());
            student.setProfile(profile);

            studentRepository.save(student);

        } else if (currentUser.getRole().getName().equals(RoleName.LECTURER)) {
            Lecturer lecturer = new Lecturer();
            lecturer.setUser(currentUser);
            lecturer.setLecturerCode(createProfileRequest.getCode());
            lecturer.setDepartment(createProfileRequest.getLecturer().getDepartment());
            lecturer.setOfficeLocation(createProfileRequest.getLecturer().getOfficeLocation());
            lecturer.setYearsOfExperience(createProfileRequest.getLecturer().getYearsOfExperience());
            lecturer.setProfile(profile);

            lecturerRepository.save(lecturer);
        } else {
            throw new RuntimeException("User does not have a valid role for profile creation");
        }

        return ResponseEntity.ok("Profile created successfully");
    }

    public ResponseEntity<?> getProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        if (currentUser.getStudent().getProfile() == null && currentUser.getLecturer().getProfile() == null) {
            return ResponseEntity.ok("Profile not found");
        }

        Profile profile = getProfileByRole(currentUser);

        ProfileResponse profileResponse = profileResponseBuilder.toDTO(profile);
        return ResponseEntity.ok(profileResponse);
    }


    @Transactional
    public ResponseEntity<?> updateProfileImage(ProfileImageType profileImageType, MultipartFile profileImage) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        Profile profile = getProfileByRole(currentUser);

        if (profileImageType == ProfileImageType.AVATAR) {
            MediaFile avatar = mediaFileService.uploadImage(profileImage);
            profile.setProfileAvatar(avatar);
        } else if (profileImageType == ProfileImageType.BACKGROUND) {
            MediaFile background = mediaFileService.uploadImage(profileImage);
            profile.setProfileBackground(background);
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
        Profile profile = getProfileByRole(currentUser);

        InformationDetail informationDetail = profile.getInformationDetail();
        if (updateInfoDetailRequest.getFullName() != null) {
            informationDetail.setFullName(updateInfoDetailRequest.getFullName());
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
        Profile profile = getProfileByRole(currentUser);

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

    @Transactional
    public ResponseEntity<?> updateProfile(UpdateProfileRequest updateProfileRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        Profile profile = getProfileByRole(currentUser);

        if (currentUser.getRole().getName().equals(RoleName.STUDENT)) {
            if (updateProfileRequest.getStudent().getStudentCode() != null) {
                profile.getStudent().setStudentCode(updateProfileRequest.getStudent().getStudentCode());
            }
            if (updateProfileRequest.getStudent().getMajor() != null) {
                profile.getStudent().setMajor(updateProfileRequest.getStudent().getMajor());
            }
            if (updateProfileRequest.getStudent().getClassName() != null) {
                profile.getStudent().setClassName(updateProfileRequest.getStudent().getClassName());
            }
            if (updateProfileRequest.getStudent().getYearOfAdmission() != null) {
                profile.getStudent().setYearOfAdmission(updateProfileRequest.getStudent().getYearOfAdmission());
            }
        } else if (currentUser.getRole().getName().equals(RoleName.LECTURER)) {
            if (updateProfileRequest.getLecturer().getLecturerCode() != null) {
                profile.getLecturer().setLecturerCode(updateProfileRequest.getLecturer().getLecturerCode());
            }
            if (updateProfileRequest.getLecturer().getDepartment() != null) {
                profile.getLecturer().setDepartment(updateProfileRequest.getLecturer().getDepartment());
            }
            if (updateProfileRequest.getLecturer().getOfficeLocation() != null) {
                profile.getLecturer().setOfficeLocation(updateProfileRequest.getLecturer().getOfficeLocation());
            }
            if (updateProfileRequest.getLecturer().getYearsOfExperience() != null) {
                profile.getLecturer().setYearsOfExperience(updateProfileRequest.getLecturer().getYearsOfExperience());
            }
        }
        if (updateProfileRequest.getNickName() != null) {
            profile.setNickName(updateProfileRequest.getNickName());
        }
        if (updateProfileRequest.getTagName() != null) {
            if (profileRepository.existsByTagName(updateProfileRequest.getTagName())) {
                throw new RuntimeException("Tag name already exists. Please choose another.");
            }
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
        Profile profile = getProfileByRole(currentUser);

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
        Profile profile = getProfileByRole(currentUser);
        List<SkillResponse> skillResponses = new SkillResponse().mapSkillsToDTOs(profile.getSkills());
        return ResponseEntity.ok(skillResponses);
    }

    public ResponseEntity<?> getProfileById(Long id) {
        Optional<Profile> profile = profileRepository.findById(id);
        if (profile.isEmpty()) {
            throw new RuntimeException("Profile not found");
        }
        ProfileResponse profileResponse = profileResponseBuilder.toDTO(profile.get());
        return ResponseEntity.ok(profileResponse);
    }

    public ResponseEntity<?> getProfileByUserId(String userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        Profile profile = getProfileByRole(user);
        ProfileResponse profileResponse = profileResponseBuilder.toDTO(profile);
        return ResponseEntity.ok(profileResponse);
    }

    public Profile getProfileByRole(User user) {
        if (user.getRole().getName().equals(RoleName.STUDENT)) {
            return profileRepository.findByStudentId(user.getStudent().getId()).orElseThrow(() -> new RuntimeException("Profile of student not found"));
        } else {
            return profileRepository.findByLecturerId(user.getLecturer().getId()).orElseThrow(() -> new RuntimeException("Profile of lecturer not found"));
        }
    }

    public Profile getProfileByUser(User user) {
        if (user.getRole().getName().equals(RoleName.STUDENT)) {
            return user.getStudent().getProfile();
        } else {
            return user.getLecturer().getProfile();
        }
    }

    public User getUserWithProfile(User user) {
        if (user.getRole().getName().equals(RoleName.STUDENT)) {
            return userRepository.findUsersWithStudentAndProfile(user.getId()).orElseThrow(() -> new RuntimeException("User not found"));
        } else {
            return userRepository.findUsersWithLecturerAndProfile(user.getId()).orElseThrow(() -> new RuntimeException("User not found"));
        }
    }
}
