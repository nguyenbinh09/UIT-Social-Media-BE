package com.example.demo.dtos.responses;

import com.example.demo.models.Topic;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TopicResponse {
    private Long id;
    private String name;
    private String description;

    public TopicResponse toDTO(Topic save) {
        this.id = save.getId();
        this.name = save.getName();
        this.description = save.getDescription();
        return this;
    }
}
