package com.example.demo.services;

import com.example.demo.dtos.responses.UserResponse;
import com.example.demo.models.User;
import com.example.demo.repositories.UserRepository;
import lombok.AllArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final ProfileService profileService;
    ProfileResponseBuilder profileResponseBuilder;

    public ResponseEntity<?> getMe() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        User currentuser = profileService.getUserWithProfile(user);

        UserResponse userResponse = new UserResponse().toDTO(currentuser, profileResponseBuilder);
        return ResponseEntity.ok(userResponse);
    }

    public ResponseEntity<?> getAllUsers() {
        return ResponseEntity.ok(new UserResponse().mapUsersToDTOs(userRepository.findAll(), profileResponseBuilder));
    }

    public ResponseEntity<?> registerFcmToken(String fcmToken) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        currentUser.setFcmToken(fcmToken);
        userRepository.save(currentUser);
        return ResponseEntity.ok("Token registered successfully");
    }

    public void removeFcmToken(User user) {
        user.setFcmToken(null);
        userRepository.save(user);
    }
}
