//package com.example.demo.models;
//
//import jakarta.persistence.*;
//import lombok.AllArgsConstructor;
//import lombok.Data;
//import lombok.EqualsAndHashCode;
//import lombok.NoArgsConstructor;
//
//@Data
//@AllArgsConstructor
//@NoArgsConstructor
//@Entity
//@Table(name = "students")
//public class Student {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @OneToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "user_id", nullable = false)
//    private User user;
//
//    @Column(name = "student_code", nullable = false)
//    private String studentCode;
//
//    @Column(name = "major")
//    private String major;
//
//    @Column(name = "class_name")
//    private String className;
//
//    @Column(name = "year_of_admission")
//    private int yearOfAdmission;
//}
