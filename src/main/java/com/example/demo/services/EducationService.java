package com.example.demo.services;

import com.example.demo.dtos.responses.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EducationService {
    private final RestTemplate restTemplate;

    @Value("${uit-education.url}")
    private String educationUrl;

    public EducationService(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    public ResponseEntity<?> loginToUniversity(String username, String password) {
        String loginUrl = educationUrl + "/api/auth/login";

        Map<String, String> payload = new HashMap<>();
        payload.put("username", username);
        payload.put("password", password);

        ResponseEntity<AuthEducationResponse> response = restTemplate.postForEntity(loginUrl, payload, AuthEducationResponse.class);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            AuthEducationResponse responseBody = response.getBody();
            return ResponseEntity.ok(responseBody);
        } else {
            throw new RuntimeException("Failed to log in to third-party server.");
        }
    }

    public ResponseEntity<?> fetchStudentProfile(String token) {
        String profileUrl = educationUrl + "/api/users/me";
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<UniversityUserResponse> response = restTemplate.exchange(
                profileUrl,
                HttpMethod.GET,
                requestEntity,
                UniversityUserResponse.class
        );

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            UniversityUserResponse responseBody = response.getBody();
            return ResponseEntity.ok(responseBody);
        } else {
            return ResponseEntity.status(response.getStatusCode())
                    .body("Failed to fetch student profile from university API.");
        }
    }

    public ResponseEntity<?> fetchScore(String token) {
        String scoreUrl = educationUrl + "/api/scores/getScores";
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<List<UniversityScoreResponse>> response = restTemplate.exchange(
                scoreUrl,
                HttpMethod.GET,
                requestEntity,
                new ParameterizedTypeReference<List<UniversityScoreResponse>>() {
                }
        );

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            List<UniversityScoreResponse> responseScores = response.getBody();
            System.out.println(responseScores);
            return ResponseEntity.ok(responseScores);
        } else {
            return ResponseEntity.status(response.getStatusCode())
                    .body("Failed to fetch student profile from university API.");
        }
    }

    public ResponseEntity<?> fetchSchedule(String token, int hocky, int namhoc) {
        String scheduleUrl = educationUrl + "/api/schedules/getSchedule";
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        try {
            UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(scheduleUrl)
                    .queryParam("hocky", hocky)
                    .queryParam("namhoc", namhoc);

            ResponseEntity<Map<Integer, List<UniversityScheduleResponse>>> response = restTemplate.exchange(
                    uriBuilder.toUriString(),
                    HttpMethod.GET,
                    requestEntity,
                    new ParameterizedTypeReference<Map<Integer, List<UniversityScheduleResponse>>>() {
                    }
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<Integer, List<UniversityScheduleResponse>> schedules = response.getBody();
                return ResponseEntity.ok(schedules);
            } else {
                return ResponseEntity.status(response.getStatusCode())
                        .body("Failed to fetch student schedule from university API.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while fetching schedule: " + e.getMessage());
        }
    }

    public ResponseEntity<?> fetchExamSchedule(String token, String examType, int hocky, int namhoc) {
        String examScheduleUrl = educationUrl + "/api/exams/getExams";
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        try {
            UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(examScheduleUrl)
                    .queryParam("examType", examType)
                    .queryParam("semester", hocky)
                    .queryParam("year", namhoc);

            ResponseEntity<List<ExamResponse>> response = restTemplate.exchange(
                    uriBuilder.toUriString(),
                    HttpMethod.GET,
                    requestEntity,
                    new ParameterizedTypeReference<List<ExamResponse>>() {
                    }
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                List<ExamResponse> examResponses = response.getBody();
                return ResponseEntity.ok(examResponses);
            } else {
                return ResponseEntity.status(response.getStatusCode())
                        .body("Failed to fetch student exam schedule from university API.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while fetching exam schedule: " + e.getMessage());
        }
    }


    public ResponseEntity<?> fetchNotification(String token, int page, int size) {
        String notificationUrl = educationUrl + "/api/notifications/getNotifications";
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        try {
            UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(notificationUrl)
                    .queryParam("page", page)
                    .queryParam("size", size);

            ResponseEntity<List<UniversityNotificationResponse>> response = restTemplate.exchange(
                    uriBuilder.toUriString(),
                    HttpMethod.GET,
                    requestEntity,
                    new ParameterizedTypeReference<List<UniversityNotificationResponse>>() {
                    }
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                List<UniversityNotificationResponse> notifications = response.getBody();
                return ResponseEntity.ok(notifications);
            } else {
                return ResponseEntity.status(response.getStatusCode())
                        .body("Failed to fetch student notifications from university API.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An error occurred while fetching notifications: " + e.getMessage());
        }
    }
}
