package com.example.demo.repositories;

import com.example.demo.models.OtpVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface OtpVerificationRepository extends JpaRepository<OtpVerification, Long> {
    Optional<OtpVerification> findByEmail(String email);

    void deleteByEmail(String email);

    @Modifying
    @Query("DELETE FROM OtpVerification o WHERE o.expiryTime < :now")
    int deleteByExpiryTimeBefore(LocalDateTime now);
}
