package com.example.demo.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UniversityLecturerResponse {
    private Long id;
    private String lecturerId;
    private String name;
    private String email;
}
