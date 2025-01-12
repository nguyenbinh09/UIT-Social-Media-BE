package com.example.demo.services;

import com.example.demo.enums.RoleName;
import com.example.demo.models.Role;
import com.example.demo.models.User;
import com.example.demo.repositories.RoleRepository;
import com.example.demo.repositories.UserRepository;
import com.github.javafaker.Faker;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class DataGenerationService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final Faker faker = new Faker();

    public void generateUserData() {
        List<User> users = new ArrayList<>();
        Role role = roleRepository.findByName(RoleName.STUDENT).orElseThrow(() -> new RuntimeException("Error: Role is not found."));

        for (int i = 0; i < 10; i++) { // Generate 1000 fake users
            User user = new User();
            String code = faker.number().digits(8);
            user.setUsername(code);
            user.setEmail(code + "@gm.uit.edu.vn");
            user.setPassword(passwordEncoder.encode("123456"));
            user.setFcmToken(faker.internet().uuid());
            user.setRole(role);
            users.add(user);
        }

        userRepository.saveAll(users);
        System.out.println("Inserted 10 fake users into the database.");
    }
}
