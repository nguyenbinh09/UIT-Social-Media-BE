package com.example.demo.dtos.responses;

import com.example.demo.models.Contact;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContactResponse {
    private Long id;
    private String email;
    private String phoneNumber;
    private String address;

    public ContactResponse toDTO(Contact contact) {
        this.setEmail(contact.getEmailToContact());
        this.setPhoneNumber(contact.getPhoneNumber());
        this.setAddress(contact.getAddress());
        return this;
    }
}
