package com.example.demo.repositories;

import com.example.demo.models.PostReaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PostReactionRepository extends JpaRepository<PostReaction, Long> {
    @Query("SELECT pr FROM PostReaction pr WHERE pr.post.id = ?1 AND pr.user.id = ?2")
    Optional<PostReaction> findByPostIdAndUserId(Long postId, String userId);

    @Query("SELECT pr FROM PostReaction pr WHERE pr.user.id = ?1 AND pr.post.id IN ?2")
    List<PostReaction> findByUserIdAndPostIdIn(String userId, List<Long> postIds);
}
