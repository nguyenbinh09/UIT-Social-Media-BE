package com.example.demo.dtos.responses;

import com.example.demo.models.Profile;
import com.example.demo.models.Student;
import com.example.demo.models.User;
import com.example.demo.services.ProfileResponseBuilder;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StudentResponse {
    private Long id;
    private String userId;
    private ProfileResponse profile;
    private String studentCode;
    private String major;
    private String className;
    private Integer yearOfAdmission;

    public StudentResponse toDTO(Student student, ProfileResponseBuilder profileResponseBuilder) {
        this.setId(student.getId());
        this.setUserId(student.getUser().getId());
        this.setProfile(profileResponseBuilder.toDTO(student.getProfile()));
        this.setStudentCode(student.getStudentCode());
        this.setMajor(student.getMajor());
        this.setClassName(student.getClassName());
        this.setYearOfAdmission(student.getYearOfAdmission());
        return this;
    }

    public StudentResponse toDTO(Student student) {
        this.setId(student.getId());
        this.setUserId(student.getUser().getId());
        this.setProfile(new ProfileResponse().toDTO(student.getProfile()));
        this.setStudentCode(student.getStudentCode());
        this.setMajor(student.getMajor());
        this.setClassName(student.getClassName());
        this.setYearOfAdmission(student.getYearOfAdmission());
        return this;
    }
}
