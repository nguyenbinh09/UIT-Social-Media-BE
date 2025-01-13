package com.example.demo.repositories;

import com.example.demo.enums.RoleName;
import com.example.demo.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    Boolean existsByUsername(String username);

    Boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u " +
            "LEFT JOIN FETCH u.student s " +
            "LEFT JOIN FETCH s.profile p " +
            "WHERE u.id = :userId")
    Optional<User> findUsersWithStudentAndProfile(@Param("userId") String userId);

    @Query("SELECT u FROM User u " +
            "LEFT JOIN FETCH u.lecturer l " +
            "LEFT JOIN FETCH l.profile p " +
            "WHERE u.id = :userId")
    Optional<User> findUsersWithLecturerAndProfile(@Param("userId") String userId);

    @Query("SELECT u FROM User u WHERE u.fcmToken = :fcmToken AND u.fcmToken IS NOT NULL")
    Optional<User> findByFcmToken(@Param("fcmToken") String fcmToken);

    @Query("SELECT u FROM User u WHERE (:role IS NULL OR u.role.name = :role)")
    Page<User> findByRole(@Param("role") RoleName role, Pageable pageable);
}
