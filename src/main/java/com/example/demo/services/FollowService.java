package com.example.demo.services;

import com.example.demo.models.Follow;
import com.example.demo.models.User;
import com.example.demo.repositories.FollowRepository;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class FollowService {
    private final FollowRepository followRepository;

    public ResponseEntity<?> followUser(String followedId) {
        // Get the current user's ID
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        String followerId = currentUser.getId();

        // Check if the follower is trying to follow themselves
        if (followerId.equals(followedId)) {
            return ResponseEntity.badRequest().body("You cannot follow yourself.");
        }

        // Check if the relationship already exists
        Optional<Follow> existingFollow = followRepository.findByFollowerIdAndFollowedId(followerId, followedId);
        if (existingFollow.isPresent()) {
            return ResponseEntity.badRequest().body("You are already following this user.");
        }

        Follow follow = new Follow();
        follow.setFollowerId(followerId);
        follow.setFollowedId(followedId);
        followRepository.save(follow);

        return ResponseEntity.ok("Successfully followed the user.");
    }

    public ResponseEntity<?> unfollowUser(String followedId) {
        // Get the current user's ID
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        String followerId = currentUser.getId();

        Optional<Follow> follow = followRepository.findByFollowerIdAndFollowedId(followerId, followedId);
        if (follow.isPresent()) {
            followRepository.delete(follow.get());
            return ResponseEntity.ok("Successfully unfollowed the user.");
        } else {
            return ResponseEntity.badRequest().body("You are not following this user.");
        }
    }

    public ResponseEntity<?> isFollowing(String followedId) {
        // Get the current user's ID
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        String followerId = currentUser.getId();

        Optional<Follow> follow = followRepository.findByFollowerIdAndFollowedId(followerId, followedId);
        if (follow.isPresent()) {
            return ResponseEntity.ok(true);
        } else {
            return ResponseEntity.ok(false);
        }
    }
}
