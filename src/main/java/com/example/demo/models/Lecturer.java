package com.example.demo.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "lecturers")
public class Lecturer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "lecturer_code", nullable = false)
    private String lecturerCode;

    @Column(name = "department")
    private String department;

    @Column(name = "office_location")
    private String officeLocation;

    @Column(name = "years_of_experience")
    private int yearsOfExperience;
}
