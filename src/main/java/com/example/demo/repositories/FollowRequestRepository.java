package com.example.demo.repositories;

import com.example.demo.models.FollowRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FollowRequestRepository extends JpaRepository<FollowRequest, Long> {
    Optional<FollowRequest> findByFollowerIdAndFollowedId(String followerId, String followedId);

    Optional<FollowRequest> findByFollowId(Long followId);
}
