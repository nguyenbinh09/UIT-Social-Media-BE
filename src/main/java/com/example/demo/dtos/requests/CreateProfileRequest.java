package com.example.demo.dtos.requests;

import com.example.demo.enums.GenderType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class CreateProfileRequest {
    private String code;
    private String nickName;
    private String tagName;
    private GenderType gender;
    private StudentRequest student;
    private LecturerRequest lecturer;
    private LocalDate birthday;
    private String phoneNumber;
    private String address;
}
