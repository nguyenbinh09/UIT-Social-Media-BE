package com.example.demo.repositories;

import com.example.demo.enums.InteractionType;
import com.example.demo.models.UserInteraction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
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

    @Query("SELECT DISTINCT ui.user.id FROM UserInteraction ui " +
            "WHERE ui.post.id IN (SELECT ui2.post.id FROM UserInteraction ui2 WHERE ui2.user.id = :userId) " +
            "AND ui.user.id <> :userId")
    List<String> findSimilarUsers(@Param("userId") String userId);

    @Query("SELECT DISTINCT t.id FROM UserInteraction ui " +
            "JOIN ui.post p " +
            "JOIN p.topics t " +
            "WHERE ui.user.id = :userId")
    List<Long> findTopicIdsByUserId(@Param("userId") String userId);

}
