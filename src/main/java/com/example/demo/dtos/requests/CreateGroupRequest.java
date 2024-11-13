package com.example.demo.dtos.requests;

import com.example.demo.models.Group;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateGroupRequest {
    private String name;
    private String description;
    private Long privacyId;
    private List<String> members;

    public Group toGroup() {
        Group group = new Group();
        group.setName(name);
        group.setDescription(description);
        return group;
    }
}
