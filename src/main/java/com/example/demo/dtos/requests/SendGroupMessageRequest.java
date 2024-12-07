package com.example.demo.dtos.requests;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class SendGroupMessageRequest {
    private Long chatGroupId;
    private String messageContent;
}
