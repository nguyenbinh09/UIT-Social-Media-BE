package com.example.demo.services;

import com.example.demo.dtos.requests.LoginUserRequest;
import com.example.demo.dtos.requests.RegisterUserRequest;
import com.example.demo.dtos.responses.AuthResponse;
import com.example.demo.models.Role;
import com.example.demo.models.User;
import com.example.demo.repositories.RoleRepository;
import com.example.demo.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@AllArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final RoleRepository roleRepository;
    private final JWTService jwtService;
    private final OtpService otpService;
    private final EmailValidationService emailValidationService;

    @Transactional
    public ResponseEntity<?> registerUser(RegisterUserRequest registerUser) {
        if (!emailValidationService.isEmailValid(registerUser.getEmail())) {
            throw new IllegalArgumentException("Invalid email.");
        }

        if (userRepository.existsByUsername(registerUser.getUsername())) {
            throw new IllegalArgumentException("Username is already taken.");
        }

        if (userRepository.existsByEmail(registerUser.getEmail())) {
            throw new IllegalArgumentException("Email is already registered.");
        }

        String otp = otpService.generateOtp();
        otpService.saveOtp(registerUser.getEmail(), otp);
        otpService.sendOtpToEmail(registerUser.getEmail(), otp);

        return ResponseEntity.ok("OTP sent to " + registerUser.getEmail());
    }

    @Transactional
    public AuthResponse verifyOtpAndRegisterUser(String otp, RegisterUserRequest registerUser) {
        if (!otpService.verifyOtp(registerUser.getEmail(), otp)) {
            throw new IllegalArgumentException("Invalid or expired OTP.");
        }

        String rawPassword = registerUser.getPassword();
        User user = new User();
        user.setUsername(registerUser.getUsername());
        user.setEmail(registerUser.getEmail());
        user.setPassword(passwordEncoder.encode(rawPassword));

        Role userRole = roleRepository.findById(registerUser.getRoleId())
                .orElseThrow(() -> new RuntimeException("Role not found"));

        user.setRoles(Collections.singletonList(userRole));
        User savedUser = userRepository.save(user);

        String accessToken = jwtService.generateToken(savedUser);
        String refreshToken = jwtService.generateRefreshToken(savedUser);

        List<String> roleNames = savedUser.getRoles().stream().map(role -> role.getName().name()).toList();
        return new AuthResponse(accessToken, refreshToken, roleNames);
    }

    public AuthResponse loginUser(LoginUserRequest loginUser) {
        Authentication authToken = new UsernamePasswordAuthenticationToken(loginUser.getUsername(), loginUser.getPassword());
        authenticationManager.authenticate(authToken);
        User user = userRepository.findByUsername(loginUser.getUsername()).orElseThrow(() -> new RuntimeException("User not found"));
        String accessToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        List<String> roleNames = user.getRoles().stream().map(role -> role.getName().name()).toList();
        return new AuthResponse(accessToken, refreshToken, roleNames);
    }

    public ResponseEntity<?> refreshToken(String refreshToken) {
        String username = jwtService.extractUsername(refreshToken);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (jwtService.validateToken(refreshToken, user)) {
            String newAccessToken = jwtService.generateToken(user);

            List<String> roleNames = user.getRoles().stream().map(role -> role.getName().name()).toList();
            return ResponseEntity.ok(new AuthResponse(newAccessToken, refreshToken, roleNames));
        }
        return ResponseEntity.badRequest().body("Invalid refresh token.");
    }
}
