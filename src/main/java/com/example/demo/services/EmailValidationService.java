package com.example.demo.services;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class EmailValidationService {
    private final List<String> validEmails = List.of("student1@gm.uit.edu.vn", "lecturer1@gm.uit.edu.vn");

    public boolean isEmailValid(String email) {
        return email.endsWith("@gm.uit.edu.vn") || validEmails.contains(email);
    }
}
