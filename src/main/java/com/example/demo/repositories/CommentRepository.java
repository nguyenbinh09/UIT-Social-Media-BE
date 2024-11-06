package com.example.demo.repositories;


import com.example.demo.models.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    @Query("SELECT c FROM Comment c WHERE c.post.id = :postId AND c.parentComment IS NULL")
    Page<Comment> findByPostIdAndParentCommentIsNull(Long postId, Pageable pageable);

    List<Comment> findByParentCommentIdIn(List<Long> parentCommentIds);

    Page<Comment> findByParentCommentId(Long parentCommentId, Pageable pageable);
}
