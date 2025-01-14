package com.example.demo.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ResolveReportRequest {
    private boolean removePost;
    private String adminNotes;
}
