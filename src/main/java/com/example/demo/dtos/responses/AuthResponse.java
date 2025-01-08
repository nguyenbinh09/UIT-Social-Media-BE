package com.example.demo.dtos.responses;

import com.example.demo.enums.RoleName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private List<String> roles;

    public AuthResponse(String accessToken, String refreshToken, List<String> roleNames) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.roles = roleNames;
    }
}
