package com.example.demo.dtos.responses;

import com.example.demo.models.Group;
import com.example.demo.models.Skill;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SkillResponse {
    private Long id;
    private String name;
    private String description;
    private Float rate;

    public SkillResponse toDTO(Skill skill) {
        SkillResponse skillResponse = new SkillResponse();
        skillResponse.setId(skill.getId());
        skillResponse.setName(skill.getName());
        skillResponse.setDescription(skill.getDescription());
        skillResponse.setRate(skill.getRate());
        return skillResponse;
    }

    public List<SkillResponse> mapSkillsToDTOs(List<Skill> skills) {
        return skills.stream()
                .map(this::toDTO)
                .toList();
    }
}
