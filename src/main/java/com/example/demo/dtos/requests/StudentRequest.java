package com.example.demo.dtos.requests;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StudentRequest {
    private String studentCode;
    private String major;
    private String className;
    private Integer yearOfAdmission;
}
