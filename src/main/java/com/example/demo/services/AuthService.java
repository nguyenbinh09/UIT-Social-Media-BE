package com.example.demo.services;

import com.example.demo.dtos.requests.LoginUserRequest;
import com.example.demo.dtos.requests.RegisterUserRequest;
import com.example.demo.dtos.responses.AuthResponse;
import com.example.demo.enums.RoleName;
import com.example.demo.models.Role;
import com.example.demo.models.User;
import com.example.demo.repositories.RoleRepository;
import com.example.demo.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final RoleRepository roleRepository;
    private final JWTService jwtService;

    @Transactional
    public AuthResponse registerUser(RegisterUserRequest registerUser) {
        String raw_password = registerUser.getPassword();
        User user = new User();
        user.setUsername(registerUser.getUsername());
        user.setEmail(registerUser.getEmail());
        user.setPassword(passwordEncoder.encode(raw_password));

        Role userRole = roleRepository.findById(registerUser.getRoleId())
                .orElseThrow(() -> new RuntimeException("Role not found"));

        List<Role> roles = new ArrayList<>();
        roles.add(userRole);
        user.setRoles(roles);
        User savedUser = userRepository.save(user);
        List<String> roleNames = savedUser.getRoles().stream().map(role -> role.getName().name()).toList();
        return new AuthResponse(jwtService.generateToken(savedUser), roleNames);
    }

    public AuthResponse loginUser(LoginUserRequest loginUser) {
        Authentication authToken = new UsernamePasswordAuthenticationToken(loginUser.getUsername(), loginUser.getPassword());
        authenticationManager.authenticate(authToken);
        User user = userRepository.findByUsername(loginUser.getUsername()).orElseThrow(() -> new RuntimeException("User not found"));
        List<String> roleNames = user.getRoles().stream().map(role -> role.getName().name()).toList();
        return new AuthResponse(jwtService.generateToken(user), roleNames);
    }
}
