package com.example.demo.dtos.requests;

import com.example.demo.enums.AccountStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AccountStatusRequest {
    private AccountStatus accountStatus;
    private String reason;
}
