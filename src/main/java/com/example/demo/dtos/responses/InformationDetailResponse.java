package com.example.demo.dtos.responses;

import com.example.demo.models.InformationDetail;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InformationDetailResponse {
    private Long id;
    private String fullName;
    private String major;
    private Integer schoolYear;
    private String activityClass;
    private List<String> work;
    private String currentCity;
    private String homeTown;

    public InformationDetailResponse toDto(InformationDetail informationDetail) {
        this.setId(informationDetail.getId());
        this.setFullName(informationDetail.getFullName());
        this.setMajor(informationDetail.getMajor());
        this.setSchoolYear(informationDetail.getSchoolYear());
        this.setActivityClass(informationDetail.getActivityClass());
        this.setWork(informationDetail.getWork());
        this.setCurrentCity(informationDetail.getCurrentCity());
        this.setHomeTown(informationDetail.getHomeTown());
        return this;
    }
}
