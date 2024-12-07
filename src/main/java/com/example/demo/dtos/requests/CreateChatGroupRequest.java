package com.example.demo.dtos.requests;

import lombok.Data;

import java.util.List;

@Data
public class CreateChatGroupRequest {
    private String chatGroupName;
    List<String> memberIds;
}
