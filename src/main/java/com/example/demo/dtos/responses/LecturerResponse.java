package com.example.demo.dtos.responses;

import com.example.demo.models.Lecturer;
import com.example.demo.models.Profile;
import com.example.demo.models.User;
import com.example.demo.services.ProfileResponseBuilder;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LecturerResponse {
    private Long id;
    private User user;
    private ProfileResponse profile;
    private String lecturerCode;
    private String department;
    private String officeLocation;
    private int yearsOfExperience;

    public LecturerResponse toDTO(Lecturer lecturer, ProfileResponseBuilder profileResponseBuilder) {
        this.setId(lecturer.getId());
        this.setUser(lecturer.getUser());
        this.setProfile(profileResponseBuilder.toDTO(lecturer.getProfile()));
        this.setLecturerCode(lecturer.getLecturerCode());
        this.setDepartment(lecturer.getDepartment());
        this.setOfficeLocation(lecturer.getOfficeLocation());
        this.setYearsOfExperience(lecturer.getYearsOfExperience());
        return this;
    }

    public LecturerResponse toDTO(Lecturer lecturer) {
        this.setId(lecturer.getId());
        this.setUser(lecturer.getUser());
        this.setProfile(new ProfileResponse().toDTO(lecturer.getProfile()));
        this.setLecturerCode(lecturer.getLecturerCode());
        this.setDepartment(lecturer.getDepartment());
        this.setOfficeLocation(lecturer.getOfficeLocation());
        this.setYearsOfExperience(lecturer.getYearsOfExperience());
        return this;
    }
}
