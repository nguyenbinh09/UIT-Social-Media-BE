package com.example.demo.repositories;

import com.example.demo.enums.InteractionType;
import com.example.demo.models.UserInteraction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserInteractionRepository extends JpaRepository<UserInteraction, Long> {
    Optional<UserInteraction> findByUserIdAndPostId(String userId, Long postId);

    Optional<UserInteraction> findByUserIdAndPostIdAndInteractionType(String userId, Long postId, InteractionType interactionType);
}
