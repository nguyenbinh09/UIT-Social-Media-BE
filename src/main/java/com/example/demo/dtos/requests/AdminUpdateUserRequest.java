package com.example.demo.dtos.requests;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AdminUpdateUserRequest {
    private String username;
    private String email;
    
}
