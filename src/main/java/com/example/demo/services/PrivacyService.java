package com.example.demo.services;

import com.example.demo.dtos.requests.CreatePrivacyRequest;
import com.example.demo.enums.PrivacyName;
import com.example.demo.models.Privacy;
import com.example.demo.repositories.PrivacyRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class PrivacyService {
    private final PrivacyRepository privacyRepository;

    public ResponseEntity<?> createPrivacy(CreatePrivacyRequest createPrivacyRequest) {
        PrivacyName privacyName;
        try {
            privacyName = PrivacyName.valueOf(createPrivacyRequest.getName());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid privacy: " + createPrivacyRequest.getName());
        }

        Optional<Privacy> privacy = privacyRepository.findByName(privacyName);
        if (privacy.isPresent()) {
            throw new RuntimeException("Privacy already exists: " + privacyName);
        }

        Privacy newPrivacy = new Privacy();
        newPrivacy.setName(privacyName);
        newPrivacy.setDescription(createPrivacyRequest.getDescription());
        return ResponseEntity.ok(privacyRepository.save(newPrivacy));
    }
}
