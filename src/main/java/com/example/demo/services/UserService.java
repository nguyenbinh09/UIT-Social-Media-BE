package com.example.demo.services;

import com.example.demo.dtos.requests.AccountStatusRequest;
import com.example.demo.dtos.requests.AdminUpdateUserRequest;
import com.example.demo.dtos.responses.CommentResponse;
import com.example.demo.dtos.responses.PendingPostResponse;
import com.example.demo.dtos.responses.PostResponse;
import com.example.demo.dtos.responses.UserResponse;
import com.example.demo.enums.PostStatus;
import com.example.demo.enums.RoleName;
import com.example.demo.models.Post;
import com.example.demo.models.User;
import com.example.demo.repositories.PostRepository;
import com.example.demo.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final ProfileService profileService;
    private final NotificationService notificationService;
    private final ProfileResponseBuilder profileResponseBuilder;
    private final PostRepository postRepository;

    public ResponseEntity<?> getMe() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        User currentuser = profileService.getUserWithProfile(user);

        UserResponse userResponse = new UserResponse().toDTO(currentuser, profileResponseBuilder);
        return ResponseEntity.ok(userResponse);
    }

    public ResponseEntity<?> getAllUsers(RoleName role, int page, int size, String sortBy, String sortDir, PagedResourcesAssembler assembler) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<User> userPage = userRepository.findByRole(role, pageable);

        List<UserResponse> userResponses = new UserResponse().mapUsersToDTOs(userPage.getContent(), profileResponseBuilder);
        Page<UserResponse> userResponsePage = new PageImpl<>(userResponses, pageable, userPage.getTotalElements());

        PagedModel<UserResponse> pagedModel = assembler.toModel(userResponsePage);
        return ResponseEntity.ok(pagedModel);
    }

    public ResponseEntity<?> registerFcmToken(String fcmToken) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        currentUser.setFcmToken(fcmToken);
        userRepository.save(currentUser);
        return ResponseEntity.ok("Token registered successfully");
    }

    @Transactional
    public ResponseEntity<?> updateAccountStatus(String userId, AccountStatusRequest accountStatusRequest) {
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setAccountStatus(accountStatusRequest.getAccountStatus());
        userRepository.save(user);
        notificationService.sendAccountStatusNotification(user, accountStatusRequest);
        return ResponseEntity.ok("Account status updated successfully");
    }

    public ResponseEntity<?> updateInfoUser(String userId, AdminUpdateUserRequest userRequest) {
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (userRequest.getUsername() != null) {
            user.setUsername(userRequest.getUsername());
        }
        if (userRequest.getEmail() != null) {
            user.setEmail(userRequest.getEmail());
        }
        userRepository.save(user);
        return ResponseEntity.ok("User info updated successfully");
    }
}
