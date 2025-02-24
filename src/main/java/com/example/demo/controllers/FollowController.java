package com.example.demo.controllers;

import com.example.demo.dtos.requests.FollowUserRequest;
import com.example.demo.enums.FollowRequestStatus;
import com.example.demo.services.FollowService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/follows")
@AllArgsConstructor
@PreAuthorize("hasRole('STUDENT') or hasRole('LECTURER')")
@SecurityRequirement(name = "bearerAuth")
public class FollowController {
    private final FollowService followService;

    @PostMapping("/follow")
    public ResponseEntity<?> followUser(@RequestParam String followedId) {
        try {

            return followService.followUser(followedId);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/unfollow")
    public ResponseEntity<?> unfollowUser(@RequestParam String followedId) {
        try {
            return followService.unfollowUser(followedId);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/get-follow-requests")
    public ResponseEntity<?> getFollowRequests() {
        try {
            return followService.getFollowRequests();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

//    @GetMapping("/isFollowing")
//    public ResponseEntity<?> isFollowing(@RequestParam String followedId) {
//        try {
//            return followService.isFollowing(followedId);
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body(e.getMessage());
//        }
//    }

    @PostMapping("/respond-follow")
    public ResponseEntity<?> respondFollowRequest(@RequestParam Long requestId, @RequestParam FollowRequestStatus responseStatus) {
        try {
            return followService.respondToFollowRequest(requestId, responseStatus);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/get-followers")
    public ResponseEntity<?> getFollowers() {
        try {
            return followService.getFollowers();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/get-following")
    public ResponseEntity<?> getFollowing() {
        try {
            return followService.getFollowing();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
