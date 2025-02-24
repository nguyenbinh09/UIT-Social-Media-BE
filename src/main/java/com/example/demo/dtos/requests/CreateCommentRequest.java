package com.example.demo.dtos.requests;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CreateCommentRequest {
    private String content;
    private Long postId;
    private Long parentId;
}
