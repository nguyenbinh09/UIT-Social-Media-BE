package com.example.demo.dtos.requests;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UpdateGroupRequest {
    private String name;
    private String description;
    private Long privacyId;
}
