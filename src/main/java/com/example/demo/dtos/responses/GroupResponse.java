package com.example.demo.dtos.responses;

import com.example.demo.models.Group;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GroupResponse {
    private Long id;
    private String name;

    public GroupResponse toDTO(Group group) {
        GroupResponse groupResponse = new GroupResponse();
        groupResponse.setId(group.getId());
        groupResponse.setName(group.getName());
        return groupResponse;
    }

    public List<GroupResponse> mapGroupsToDTOs(List<Group> groups) {
        return groups.stream()
                .map(this::toDTO)
                .toList();
    }
}
