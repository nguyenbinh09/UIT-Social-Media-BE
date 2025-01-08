package com.example.demo.dtos.responses;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class UniversityStudentResponse {
    private Long id;
    private String sid;
    private Integer course;
    @JsonProperty("profile")
    private UniversityProfileResponse profile;

    public UniversityStudentResponse() {
    }

    public UniversityStudentResponse(Long id, String sid, Integer course, UniversityProfileResponse profile) {
        this.id = id;
        this.sid = sid;
        this.course = course;
        this.profile = profile;
    }
}
