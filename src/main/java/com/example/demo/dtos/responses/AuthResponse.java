package com.example.demo.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String type = "Bearer";
    private List<String> roles;

    public AuthResponse(String token, List<String> roleNames) {
        this.token = token;
        this.roles = roleNames;
    }
}
