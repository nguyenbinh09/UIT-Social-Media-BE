package com.example.demo.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class UniversityNotificationResponse {
    private Long id;
    private String title;
    private String sid;
    private String content;
    private String type;
    private String member;
    private LocalDateTime dated;
    private int hocky;
    private int namhoc;
}
