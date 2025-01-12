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
    private String role;

    public AuthResponse(String accessToken, String refreshToken, String roleName) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.role = roleName;
    }
}
