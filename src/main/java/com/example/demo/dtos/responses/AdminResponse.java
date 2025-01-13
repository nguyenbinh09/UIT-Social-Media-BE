package com.example.demo.dtos.responses;

import com.example.demo.models.Admin;
import com.example.demo.models.Contact;
import com.example.demo.models.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminResponse {
    private Long id;
    private String userId;
    private List<String> permissions;
    private String adminCode;
    private ContactResponse contact;

    public AdminResponse toDTO(Admin admin) {
        this.setId(admin.getId());
        this.setUserId(admin.getUser().getId());
        this.setPermissions(admin.getPermissions());
        this.setAdminCode(admin.getAdminCode());
        this.setContact(new ContactResponse().toDTO(admin.getContact()));
        return this;
    }
}
