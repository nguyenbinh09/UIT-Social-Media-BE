package com.example.demo.dtos.requests;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReactPostRequest {
    private Long postId;
    private Long reactionTypeId;
}
