package com.example.demo.repositories;

import com.example.demo.enums.PrivacyName;
import com.example.demo.models.Privacy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PrivacyRepository extends JpaRepository<Privacy, Long> {
    Optional<Privacy> findByName(PrivacyName name);
}
