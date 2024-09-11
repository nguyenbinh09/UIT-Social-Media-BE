package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")  // Enable auditing and provide auditor

public class UitAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(UitAppApplication.class, args);
    }

}
