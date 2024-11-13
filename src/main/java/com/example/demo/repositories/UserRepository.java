package com.example.demo.repositories;

import com.example.demo.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByUsername(String username);

    @Query("SELECT u.fcmToken FROM User u WHERE u.id = :userId")
    String findFcmTokenByUserId(String userId);

    List<User> findAllByIdIn(List<String> userIds);

    @Query("SELECT u FROM User u WHERE u.id IN :userIds")
    Page<User> findAllUsersByIdIn(List<String> userIds, Pageable pageable);

    List<User> findAllUsersByIdIn(List<String> userIds);
}
