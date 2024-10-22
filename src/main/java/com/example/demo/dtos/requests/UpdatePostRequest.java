package com.example.demo.dtos.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.checkerframework.checker.units.qual.A;

@Data
@AllArgsConstructor
public class UpdatePostRequest {
    private Long id;
    private String title;
    private String textContent;
    private Long privacyId;
}
