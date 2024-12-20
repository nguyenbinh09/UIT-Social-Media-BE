package com.example.demo.init;

import com.example.demo.enums.RoleName;
import com.example.demo.models.Role;
import com.example.demo.repositories.RoleRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AdminRoleInitializer {
    @Autowired
    private RoleRepository roleRepository;

    @PostConstruct
    public void initAdminRole() {
        if (!roleRepository.existsByName(RoleName.ADMIN)) {
            Role adminRole = new Role();
            adminRole.setName(RoleName.ADMIN);
            roleRepository.save(adminRole);
        }
    }
}