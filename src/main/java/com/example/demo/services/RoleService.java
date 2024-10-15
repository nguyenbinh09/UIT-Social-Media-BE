package com.example.demo.services;

import com.example.demo.dtos.requests.CreateRoleRequest;
import com.example.demo.enums.RoleName;
import com.example.demo.models.Role;
import com.example.demo.repositories.RoleRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class RoleService {
    private final RoleRepository roleRepository;

    public ResponseEntity<?> createRole(CreateRoleRequest roleRequest) {
        String roleNameString = roleRequest.getName();
        RoleName roleNameEnum;
        try {
            roleNameEnum = RoleName.valueOf(roleNameString);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid role: " + roleNameString);
        }
        Optional<Role> role = roleRepository.findByName(roleNameEnum);
        if (role.isPresent()) {
            return ResponseEntity.badRequest().body("Role already exists");
        }
        Role newRole = new Role();
        newRole.setName(roleNameEnum);
        return ResponseEntity.ok(roleRepository.save(newRole));
    }
}
