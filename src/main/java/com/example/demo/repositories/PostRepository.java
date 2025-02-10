package com.example.demo.repositories;

import com.example.demo.enums.PostStatus;
import com.example.demo.models.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.net.ContentHandler;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    @Query("SELECT p FROM Post p WHERE p.user.id IN :userIds AND p.isDeleted = false AND p.privacy.name = 'PUBLIC'")
    Page<Post> findByUserIdsAndIsDeletedAndPrivacy(
            @Param("userIds") List<String> userIds,
            Pageable pageable);

    Page<Post> findByGroupId(Long groupId, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.group.id = :groupId AND p.isDeleted = false AND p.privacy.name = 'PUBLIC' AND p.status = 'APPROVED'")
    List<Post> findByGroupIdAndIsDeleted(Long groupId, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.user.id = :id AND p.isDeleted = false AND p.privacy.name = 'PUBLIC' AND p.status = 'APPROVED'")
    List<Post> findByUserId(String id, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.user.id = :id AND p.isDeleted = false AND p.status = 'APPROVED'")
    List<Post> findByCurrentUserId(String id, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.status = :postStatus AND p.group.id IS NULL")
    List<Post> findByStatusWithoutGroupId(@Param("postStatus") PostStatus postStatus, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.status = :postStatus AND p.updatedAt < :threshold")
    List<Post> findAllByStatusAndUpdatedAtBefore(PostStatus postStatus, LocalDateTime threshold);

    @Query("SELECT p FROM Post p JOIN p.topics t WHERE t.id = :topicId AND p.isDeleted = false AND p.privacy.name = 'PUBLIC' AND p.status = 'APPROVED'")
    List<Post> findByTopicId(@Param("topicId") Long topicId, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.isDeleted = false AND p.privacy.name = 'PUBLIC' AND p.status = 'APPROVED'")
    List<Post> findAllWithStatus(Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.id = :postId AND p.isDeleted = false AND p.group.id IS NULL")
    Optional<Post> findByIdWithoutGroupId(Long postId);

    @Query("SELECT p FROM Post p WHERE p.status = :postStatus AND p.group.id = :groupId")
    List<Post> findByStatusAndGroupId(PostStatus postStatus, Long groupId, Pageable pageable);

    @Query("SELECT COUNT(p) FROM Post p WHERE p.createdAt BETWEEN :startDate AND :endDate")
    long countPostsBetweenDates(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}
