package com.example.demo.dtos.requests;

import com.example.demo.enums.GenderType;
import com.example.demo.models.MediaFile;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class UpdateProfileRequest {
    private String studentCode;
    private String nickName;
    private String tagName;
    private LocalDate birthDate;
    private GenderType gender;
    private String bio;
    private Boolean isPrivate;
}
