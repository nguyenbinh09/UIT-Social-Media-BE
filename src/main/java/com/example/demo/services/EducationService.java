package com.example.demo.services;

import com.example.demo.dtos.responses.AuthEducationResponse;
import com.example.demo.dtos.responses.UniversityScoreResponse;
import com.example.demo.dtos.responses.UniversityUserResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class EducationService {
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public EducationService(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    public ResponseEntity<?> loginToUniversity(String username, String password) {
        String loginUrl = "http://localhost:8082/api/auth/login";
//        String loginUrl = "https://uit-be-simulator.onrender.com/api/auth/login";

        Map<String, String> payload = new HashMap<>();
        payload.put("username", username);
        payload.put("password", password);

        ResponseEntity<Map> response = restTemplate.postForEntity(loginUrl, payload, Map.class);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            Map<String, Object> responseBody = response.getBody();

            AuthEducationResponse authResponse = objectMapper.convertValue(responseBody, AuthEducationResponse.class);

            return ResponseEntity.ok(authResponse);
        } else {
            throw new RuntimeException("Failed to log in to third-party server.");
        }
    }

    public ResponseEntity<?> fetchStudentProfile(String token) {
        String profileUrl = "http://localhost:8082/api/users/me";
//        String profileUrl = "https://uit-be-simulator.onrender.com/api/users/me";
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                profileUrl,
                HttpMethod.GET,
                requestEntity,
                Map.class
        );

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            Map<String, Object> responseBody = response.getBody();
            UniversityUserResponse userResponse = objectMapper.convertValue(responseBody, UniversityUserResponse.class);
            return ResponseEntity.ok(userResponse);
        } else {
            return ResponseEntity.status(response.getStatusCode())
                    .body("Failed to fetch student profile from university API.");
        }
    }

    public ResponseEntity<?> fetchScore(String token) {
        String scoreUrl = "http://localhost:8082/api/scores/getScores";
//        String scoreUrl = "https://uit-be-simulator.onrender.com/api/scores/getScores";
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                scoreUrl,
                HttpMethod.GET,
                requestEntity,
                Map.class
        );

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            Map<String, Object> responseBody = response.getBody();
            UniversityScoreResponse scoreResponse = objectMapper.convertValue(responseBody, UniversityScoreResponse.class);
            return ResponseEntity.ok(scoreResponse);
        } else {
            return ResponseEntity.status(response.getStatusCode())
                    .body("Failed to fetch student profile from university API.");
        }
    }
}
