package com.example.demo.dtos.responses;

import com.example.demo.models.Group;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupResponse {
    private Long id;
    private String name;

    public GroupResponse toDTO(Group group) {
        this.setId(group.getId());
        this.setName(group.getName());
        return this;
    }
}
