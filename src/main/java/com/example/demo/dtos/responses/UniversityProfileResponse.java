package com.example.demo.dtos.responses;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
public class UniversityProfileResponse {
    private String name;
    private String email;
    private Integer status;
    private String major;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dob;
    private String role;
    private String className;
    private String address;
    private String avatarUrl;

    public UniversityProfileResponse() {
    }

    public UniversityProfileResponse(String name, String email, Integer status, String major, LocalDate dob, String role, String className, String address, String avatarUrl) {
        this.name = name;
        this.email = email;
        this.status = status;
        this.major = major;
        this.dob = dob;
        this.role = role;
        this.className = className;
        this.address = address;
        this.avatarUrl = avatarUrl;
    }
}
