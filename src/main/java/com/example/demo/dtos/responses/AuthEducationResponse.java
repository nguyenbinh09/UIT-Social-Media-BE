package com.example.demo.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
public class AuthEducationResponse {
    private String token;
    private Date expireTime;
    private String type = "Bearer";

    public AuthEducationResponse(String token, Date expireTime, String type) {
        this.token = token;
        this.expireTime = expireTime;
        this.type = type;
    }
}
