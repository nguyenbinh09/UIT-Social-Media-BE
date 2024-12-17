package com.example.demo.services;

import com.example.demo.dtos.responses.FollowRequestResponse;
import com.example.demo.enums.FollowRequestStatus;
import com.example.demo.models.Follow;
import com.example.demo.models.FollowRequest;
import com.example.demo.models.User;
import com.example.demo.repositories.FollowRepository;
import com.example.demo.repositories.FollowRequestRepository;
import com.example.demo.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class FollowService {
    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final FollowRequestRepository followRequestRepository;

    @Transactional
    public ResponseEntity<?> followUser(String followedId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        User followedUser = userRepository.findById(followedId).orElseThrow(() -> new RuntimeException("User not found"));

        if (currentUser.getId().equals(followedId)) {
            return ResponseEntity.badRequest().body("You cannot follow yourself.");
        }

        Optional<Follow> existingFollow = followRepository.findByFollowerIdAndFollowedId(currentUser.getId(), followedId);
        if (existingFollow.isPresent()) {
            return ResponseEntity.badRequest().body("You are already following this user.");
        }

        Follow follow = new Follow();
        follow.setFollower(currentUser);
        follow.setFollowed(followedUser);
        followRepository.save(follow);

        Optional<Follow> existingFollowOfRequest = followRepository.findByFollowerIdAndFollowedId(followedId, currentUser.getId());

        if (existingFollowOfRequest.isPresent()) {
            Optional<FollowRequest> followRequest = followRequestRepository.findByFollowId(existingFollowOfRequest.get().getId());
            if (followRequest.isPresent()) {
                followRequest.get().setStatus(FollowRequestStatus.ACCEPTED);
                followRequestRepository.save(followRequest.get());
            }
        } else {
            FollowRequest newFollowRequest = new FollowRequest();
            newFollowRequest.setFollower(currentUser);
            newFollowRequest.setFollowed(followedUser);
            newFollowRequest.setStatus(FollowRequestStatus.PENDING);
            newFollowRequest.setFollow(follow);
            followRequestRepository.save(newFollowRequest);

            //        notificationService.sendNotification(follow.getFollowed().getFcmToken(), "Follow Request", currentUser.getUsername() + " sent you a follow request.");
        }
        return ResponseEntity.ok("Successfully followed the user.");
    }

    @Transactional
    public ResponseEntity<?> unfollowUser(String followedId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        String followerId = currentUser.getId();

        Optional<Follow> follow = followRepository.findByFollowerIdAndFollowedId(followerId, followedId);
        if (follow.isPresent()) {
            Optional<FollowRequest> followRequest = followRequestRepository.findByFollowId(follow.get().getId());
            followRequest.ifPresent(followRequestRepository::delete);
            followRepository.delete(follow.get());
            return ResponseEntity.ok("Successfully unfollowed the user.");
        } else {
            return ResponseEntity.badRequest().body("You are not following this user.");
        }
    }

    public Boolean isFollowing(String followerId, String followedId) {
        Optional<Follow> follow = followRepository.findByFollowerIdAndFollowedId(followerId, followedId);
        return follow.isPresent();
    }

    @Transactional
    public ResponseEntity<?> respondToFollowRequest(Long requestId, FollowRequestStatus responseStatus) {
        FollowRequest followRequest = followRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Follow request not found"));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        if (!followRequest.getFollowed().getId().equals(currentUser.getId())) {
            return ResponseEntity.badRequest().body("You are not authorized to respond to this follow request.");
        }
        Optional<Follow> existingFollow = followRepository.findByFollowerIdAndFollowedId(followRequest.getFollower().getId(), currentUser.getId());
        if (existingFollow.isPresent()) {
            if (responseStatus == FollowRequestStatus.ACCEPTED && followRequest.getStatus() == FollowRequestStatus.PENDING) {
                followRequest.setStatus(FollowRequestStatus.ACCEPTED);
                followRequestRepository.save(followRequest);
                User followedUser = userRepository.findById(followRequest.getFollower().getId())
                        .orElseThrow(() -> new RuntimeException("Follower not found"));

                Follow follow = new Follow();
                follow.setFollower(currentUser);
                follow.setFollowed(followedUser);
                followRepository.save(follow);

                return ResponseEntity.ok("Follow request accepted.");
            } else if (responseStatus == FollowRequestStatus.REJECTED && followRequest.getStatus() == FollowRequestStatus.PENDING) {
                followRequest.setStatus(FollowRequestStatus.REJECTED);
                followRequestRepository.save(followRequest);
                return ResponseEntity.ok("Follow request rejected.");
            }
        } else {
            return ResponseEntity.badRequest().body("The follower is not following you.");
        }
        return ResponseEntity.badRequest().body("Invalid response status.");
    }

    public ResponseEntity<?> getFollowRequests() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        List<FollowRequest> followRequests = followRequestRepository.findByFollowedIdAndStatus(currentUser.getId(), FollowRequestStatus.PENDING);
        List<FollowRequestResponse> followRequestResponses = followRequests.stream()
                .map(followRequest -> new FollowRequestResponse().toDTO(followRequest))
                .toList();
        return ResponseEntity.ok(followRequestResponses);
    }
}
