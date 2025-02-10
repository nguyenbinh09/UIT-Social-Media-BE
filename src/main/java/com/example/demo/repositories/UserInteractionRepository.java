package com.example.demo.repositories;

import com.example.demo.enums.InteractionType;
import com.example.demo.models.UserInteraction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UserInteractionRepository extends JpaRepository<UserInteraction, Long> {
    Optional<UserInteraction> findByUserIdAndPostId(String userId, Long postId);

    Optional<UserInteraction> findByUserIdAndPostIdAndInteractionType(String userId, Long postId, InteractionType interactionType);

    @Query("SELECT COUNT(ui) FROM UserInteraction ui WHERE ui.interactionType = :interactionType " +
            "AND ui.timestamp BETWEEN :startDate AND :endDate")
    long countInteractionsBetweenDates(@Param("interactionType") InteractionType interactionType,
                                       @Param("startDate") LocalDateTime startDate,
                                       @Param("endDate") LocalDateTime endDate);
}
