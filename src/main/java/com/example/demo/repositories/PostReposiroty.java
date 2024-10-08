package com.example.demo.repositories;

import com.example.demo.models.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.util.List;

@Repository
public interface PostReposiroty extends JpaRepository<Post, Long> {
    Page<Post> findByUserIdIn(List<String> userIds, Pageable pageable);
}
