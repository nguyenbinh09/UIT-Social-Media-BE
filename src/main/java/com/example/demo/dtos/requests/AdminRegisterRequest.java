package com.example.demo.dtos.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminRegisterRequest {
    private String username;
    private String email;
    private String password;
    private String phone;
    private String address;
    private List<Long> permissions;
}
