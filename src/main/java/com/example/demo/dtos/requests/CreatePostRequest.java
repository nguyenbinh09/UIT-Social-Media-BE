package com.example.demo.dtos.requests;

import com.example.demo.models.Post;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class CreatePostRequest {
    private String textContent;
    private String title;
    private Long privacyId;
    private String link;
    private List<Long> topicIds;
}
