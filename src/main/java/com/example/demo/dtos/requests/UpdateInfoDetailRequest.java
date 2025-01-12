package com.example.demo.dtos.requests;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class UpdateInfoDetailRequest {
    private String fullName;
    private List<String> work;
    private String currentCity;
    private String homeTown;
}
