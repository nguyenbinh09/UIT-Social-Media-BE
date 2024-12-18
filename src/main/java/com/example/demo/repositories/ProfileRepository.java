package com.example.demo.repositories;

import com.example.demo.models.Profile;
import com.example.demo.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, Long> {
    boolean existsByTagName(String tagName);
}
