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

    @Query("SELECT p FROM Post p WHERE p.user.id IN " +
            "(SELECT f.follower.id FROM Follow f WHERE f.followed.id = :userId " +
            "AND f.follower.id IN (SELECT f2.followed.id FROM Follow f2 WHERE f2.follower.id = :userId)) " +
            "AND p.isDeleted = false ORDER BY p.createdAt DESC")
    List<Post> findFriendPosts(@Param("userId") String userId, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.user.id IN " +
            "(SELECT f.followed.id FROM Follow f WHERE f.follower.id = :userId) " +
            "AND p.isDeleted = false ORDER BY p.createdAt DESC")
    List<Post> findFollowedPosts(@Param("userId") String userId, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.group.id IN " +
            "(SELECT gm.group.id FROM GroupMembership gm WHERE gm.user.id = :userId) " +
            "AND p.isDeleted = false ORDER BY p.createdAt DESC")
    List<Post> findGroupPosts(@Param("userId") String userId, Pageable pageable);

    @Query("SELECT p FROM Post p " +
            "WHERE p.createdAt >= :time " +
            "ORDER BY SIZE(p.reactions) DESC, p.createdAt DESC")
    List<Post> findTrendingPosts(@Param("time") LocalDateTime time, Pageable pageable);

    @Query("SELECT DISTINCT p FROM Post p " +
            "JOIN p.topics t " +
            "WHERE t.id IN :topicIds " +
            "ORDER BY p.createdAt DESC")
    List<Post> findPostsByTopicIds(@Param("topicIds") List<Long> topicIds, Pageable pageable);

    @Query("SELECT DISTINCT p FROM Post p " +
            "WHERE p.user.id IN :userIds " +
            "ORDER BY p.createdAt DESC")
    List<Post> findPostsByUserIds(@Param("userIds") List<String> userIds, Pageable pageable);

}
