package com.example.demo.dtos.requests;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UpdateChatGroupRequest {
    private String name;
    private Boolean isInvited;
}
