package com.example.demo.repositories;

import com.example.demo.models.Profile;
import com.example.demo.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, Long> {
    boolean existsByTagName(String tagName);

    Optional<Profile> findByStudentId(Long id);

    Optional<Profile> findByLecturerId(Long id);
}
