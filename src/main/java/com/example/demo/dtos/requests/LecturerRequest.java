package com.example.demo.dtos.requests;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LecturerRequest {
    private String lecturerCode;
    private String department;
    private String officeLocation;
    private Integer yearsOfExperience;
}
