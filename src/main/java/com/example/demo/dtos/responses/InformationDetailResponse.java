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
    private List<String> work;
    private String currentCity;
    private String homeTown;

    public InformationDetailResponse toDto(InformationDetail informationDetail) {
        this.setId(informationDetail.getId());
        this.setFullName(informationDetail.getFullName());
        this.setWork(informationDetail.getWork());
        this.setCurrentCity(informationDetail.getCurrentCity());
        this.setHomeTown(informationDetail.getHomeTown());
        return this;
    }
}
