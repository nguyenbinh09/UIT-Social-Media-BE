package com.example.demo.dtos.requests;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UpdateContactRequest {
    private String emailToContact;
    private String phoneNumber;
    private String address;
}
