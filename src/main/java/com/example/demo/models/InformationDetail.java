package com.example.demo.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "information_details")
public class InformationDetail extends BaseModel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "major", nullable = false)
    private String major;

    @Column(name = "school_year", nullable = false)
    private Integer schoolYear;

    @Column(name = "activity_class", nullable = false)
    private String activityClass;

    @Column(name = "work")
    private List<String> work;

    @Column(name = "current_city")
    private String currentCity;

    @Column(name = "home_town")
    private String homeTown;
}
