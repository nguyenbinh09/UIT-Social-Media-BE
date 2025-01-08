package com.example.demo.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
public class UniversityNotificationResponse {
    private String id;
    private String title;
    private String sid;
    private String content;
    private String type;
    private String member;
    private String dated;
    private String hocky;
    private String namhoc;
}
