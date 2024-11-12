package com.example.demo.repositories;

import com.example.demo.models.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    @Query("SELECT p FROM Post p WHERE p.user.id IN :userIds AND p.isDeleted = false AND p.privacy.name = 'PUBLIC'")
    Page<Post> findByUserIdsAndIsDeletedAndPrivacy(
            @Param("userIds") List<String> userIds,
            Pageable pageable);

    Page<Post> findByGroupId(Long groupId, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.group.id = :groupId AND p.isDeleted = false")
    List<Post> findByGroupIdAndIsDeleted(Long groupId, Pageable pageable);
}
