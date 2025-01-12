package com.example.demo.dtos.responses;


import com.example.demo.enums.RoleName;
import com.example.demo.models.Student;
import com.example.demo.models.User;
import com.example.demo.repositories.ProfileRepository;
import com.example.demo.services.ProfileResponseBuilder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {
    private String id;
    private String username;
    private String email;
    private StudentResponse student;
    private LecturerResponse lecturer;
    private RoleName role;

    public UserResponse toDTO(User user, ProfileResponseBuilder profileResponseBuilder) {
        UserResponse userResponse = new UserResponse();
        userResponse.setId(user.getId());
        userResponse.setUsername(user.getUsername());
        if (user.getRole().getName().equals(RoleName.STUDENT)) {
            userResponse.setStudent(new StudentResponse().toDTO(user.getStudent(), profileResponseBuilder));
        } else if (user.getRole().getName().equals(RoleName.LECTURER)) {
            userResponse.setLecturer(new LecturerResponse().toDTO(user.getLecturer(), profileResponseBuilder));
        }
        userResponse.setEmail(user.getEmail());
        userResponse.setRole(user.getRole().getName());
        return userResponse;
    }

//    public UserResponse toDTO(User user) {
//        UserResponse userResponse = new UserResponse();
//        userResponse.setId(user.getId());
//        userResponse.setUsername(user.getUsername());
//        if (user.getRole().getName().equals(RoleName.STUDENT)) {
//            userResponse.setStudent(new StudentResponse().toDTO(user.getStudent()));
//        } else if (user.getRole().getName().equals(RoleName.LECTURER)) {
//            userResponse.setLecturer(new LecturerResponse().toDTO(user.getLecturer()));
//        }
//        userResponse.setEmail(user.getEmail());
//        userResponse.setRole(user.getRole().getName());
//        return userResponse;
//    }

    public List<UserResponse> mapUsersToDTOs(List<User> users, ProfileResponseBuilder profileResponseBuilder) {
        return users.stream()
                .map(user -> new UserResponse().toDTO(user, profileResponseBuilder))
                .collect(Collectors.toList());
    }
}
