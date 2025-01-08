package com.example.demo.controllers;

import com.example.demo.dtos.requests.LoginUserRequest;
import com.example.demo.dtos.requests.RefreshTokenRequest;
import com.example.demo.dtos.requests.RegisterUserRequest;
import com.example.demo.dtos.responses.AuthResponse;
import com.example.demo.models.User;
import com.example.demo.services.AuthService;
import com.example.demo.services.EmailValidationService;
import com.example.demo.services.JWTService;
import com.example.demo.services.OtpService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@AllArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final OtpService otpService;
    private final EmailValidationService emailValidationService;

    @CrossOrigin
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginUserRequest loginUserRequest) {
        try {
            AuthResponse response = authService.loginUser(loginUserRequest);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterUserRequest registerUserRequest) {
        try {
            return authService.registerUser(registerUserRequest);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/verify-and-register")
    public ResponseEntity<?> verifyAndRegister(@RequestBody RegisterUserRequest registerUserRequest, @RequestParam String otp) {
        try {
            AuthResponse authResponse = authService.verifyOtpAndRegisterUser(otp, registerUserRequest);
            return ResponseEntity.ok(authResponse);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<?> resendOtp(@RequestParam String email) {
        try {
            return otpService.resendOtp(email);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequest refreshTokenRequest) {
        String refreshToken = refreshTokenRequest.getRefreshToken();

        try {
            return authService.refreshToken(refreshToken);
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }
}
