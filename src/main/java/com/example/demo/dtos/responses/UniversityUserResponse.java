package com.example.demo.dtos.responses;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class UniversityUserResponse {
    private String id;
    private String username;
    private String email;
    @JsonProperty("student")
    private UniversityStudentResponse student;

    public UniversityUserResponse() {
    }

    public UniversityUserResponse(String id, String username, String email, UniversityStudentResponse student) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.student = student;
    }
}
