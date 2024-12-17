package com.example.demo.dtos.requests;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SharePostRequest {
    private Long originalPostId;
    private String additionalContent;
    private String title;
    private Long privacyId;
}
