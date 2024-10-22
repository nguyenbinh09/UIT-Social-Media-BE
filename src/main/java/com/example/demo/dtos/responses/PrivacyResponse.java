package com.example.demo.dtos.responses;


import com.example.demo.enums.PrivacyName;
import com.example.demo.models.Privacy;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PrivacyResponse {
    private Long id;
    private PrivacyName name;
    private String description;

    public PrivacyResponse toDTO(Privacy privacy) {
        PrivacyResponse privacyResponse = new PrivacyResponse();
        privacyResponse.setId(privacy.getId());
        privacyResponse.setName(privacy.getName());
        privacyResponse.setDescription(privacy.getDescription());
        return privacyResponse;
    }
}