package com.example.demo.repositories;

import com.example.demo.models.SavedPost;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SavedPostRepository extends JpaRepository<SavedPost, Long> {
    List<SavedPost> findByUserId(String userId, Pageable pageable);

    boolean existsByUserIdAndPostId(String userId, Long postId);

    void deleteByUserIdAndPostId(String userId, Long postId);
}
