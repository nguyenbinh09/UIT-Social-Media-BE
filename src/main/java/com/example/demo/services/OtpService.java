package com.example.demo.services;

import com.example.demo.models.OtpVerification;
import com.example.demo.models.User;
import com.example.demo.repositories.OtpVerificationRepository;
import com.example.demo.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
@AllArgsConstructor
public class OtpService {
    private final OtpVerificationRepository otpRepository;
    private final EmailService emailService;
    private final UserRepository userRepository;

    public String generateOtp() {
        return String.format("%06d", new Random().nextInt(999999));
    }

    public void sendOtpToEmail(String email, String otp) {
        emailService.sendOtpEmail(email, "Your OTP Code", "Your OTP is: " + otp);
    }

    public void saveOtp(String email, String otp) {
        otpRepository.deleteByEmail(email);

        OtpVerification otpVerification = new OtpVerification();
        otpVerification.setEmail(email);
        otpVerification.setOtp(otp);
        otpVerification.setExpiryTime(LocalDateTime.now().plusMinutes(5));

        otpRepository.save(otpVerification);
    }

    public boolean verifyOtp(String email, String otp) {
        Optional<OtpVerification> otpVerification = otpRepository.findByEmail(email);

        if (otpVerification.isPresent() &&
                otpVerification.get().getOtp().equals(otp) &&
                otpVerification.get().getExpiryTime().isAfter(LocalDateTime.now())) {
            otpRepository.deleteByEmail(email);
            return true;
        }
        return false;
    }

    public ResponseEntity<?> resendOtp(String email) {
        Optional<OtpVerification> existingOtp = otpRepository.findByEmail(email);
        if (existingOtp.isEmpty()) {
            throw new RuntimeException("Email hasn't been required OTP yet or email is not existed in the system.");
        }

        String newOtp = generateOtp();
        saveOtp(email, newOtp);

        emailService.sendOtpEmail(email, "Your Resent OTP Code", "Your new OTP is: " + newOtp);
        return ResponseEntity.ok("OTP resent to " + email);
    }

    @Scheduled(fixedRate = 3600000)
    @Transactional
    public void cleanExpiredOtps() {
        LocalDateTime now = LocalDateTime.now();
        int deletedCount = otpRepository.deleteByExpiryTimeBefore(now);
        System.out.println("Removed " + deletedCount + " expired OTP.");
    }
}