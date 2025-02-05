package com.example.demo.services;

import com.example.demo.dtos.requests.AccountStatusRequest;
import com.example.demo.dtos.requests.AdminRegisterRequest;
import com.example.demo.dtos.requests.AdminUpdateUserRequest;
import com.example.demo.dtos.responses.UserResponse;
import com.example.demo.enums.AccountStatus;
import com.example.demo.enums.RoleName;
import com.example.demo.models.*;
import com.example.demo.repositories.*;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final ProfileService profileService;
    private final NotificationService notificationService;
    private final ProfileResponseBuilder profileResponseBuilder;
    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final ContactRepository contactRepository;
    private final PermissionRepository permissionRepository;

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

    public ResponseEntity<?> registerAdmin(AdminRegisterRequest adminRegisterRequest) {
        if (userRepository.existsByUsername(adminRegisterRequest.getUsername())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Username already exists.");
        }
        Role role = roleRepository.findByName(RoleName.ADMIN)
                .orElseThrow(() -> new IllegalArgumentException("Role not found"));
        List<Permission> permissions = permissionRepository.findAllById(adminRegisterRequest.getPermissions());
        if (permissions.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("You haven't provide any permissions or permissions not found.");
        }
        User user = new User();
        user.setUsername(adminRegisterRequest.getUsername());
        user.setPassword(passwordEncoder.encode(adminRegisterRequest.getPassword()));
        user.setEmail(adminRegisterRequest.getEmail());
        user.setRole(role);
        userRepository.save(user);

        Contact contact = new Contact();
        contact.setEmailToContact(adminRegisterRequest.getEmail());
        contact.setPhoneNumber(adminRegisterRequest.getPhone());
        contact.setAddress(adminRegisterRequest.getAddress());
        contactRepository.save(contact);

        String adminCode = generateAdminCode();

        Admin admin = new Admin();
        admin.setUser(user);
        admin.setAdminCode(adminCode);
        admin.setPermissions(permissions);
        admin.setContact(contact);
        adminRepository.save(admin);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Admin account created successfully.");
    }

    private String generateAdminCode() {
        long adminCount = adminRepository.count();
        long nextAdminCode = adminCount + 1;
        return String.format("AD%04d", nextAdminCode);
    }

    public ResponseEntity<?> getFirstAdmin() {
        User firstAdmin = userRepository.findFirstByRoleName(RoleName.ADMIN)
                .orElseThrow(() -> new IllegalArgumentException("Admin not found"));
        UserResponse userResponse = new UserResponse().toDTO(firstAdmin, profileResponseBuilder);
        return ResponseEntity.ok(userResponse);
    }
}
