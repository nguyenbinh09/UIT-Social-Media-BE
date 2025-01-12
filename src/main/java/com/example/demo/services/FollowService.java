package com.example.demo.services;

import com.example.demo.dtos.responses.FollowRequestResponse;
import com.example.demo.dtos.responses.UserResponse;
import com.example.demo.enums.FollowRequestStatus;
import com.example.demo.enums.NotificationType;
import com.example.demo.models.*;
import com.example.demo.repositories.*;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Service
@AllArgsConstructor
public class FollowService {
    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    private final FollowRequestRepository followRequestRepository;
    private final FirebaseService firebaseService;
    private final NotificationRepository notificationRepository;
    private final ProfileService profileService;
    private final ProfileResponseBuilder profileResponseBuilder;

    @Transactional
    public ResponseEntity<?> followUser(String followedId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        User currentUser = profileService.getUserWithProfile(user);
        User followedUser = profileService.getUserWithProfile(userRepository.findById(followedId)
                .orElseThrow(() -> new RuntimeException("User not found")));

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

            String message = currentUser.getUsername() + " has followed you. Do you want to respond the follow request?";
            String actionUrl = "/follow-requests/" + newFollowRequest.getId();

            Notification notification = new Notification();
            notification.setSender(currentUser);
            notification.setReceiver(followedUser);
            notification.setType(NotificationType.FOLLOW_REQUEST);
            notification.setMessage(message);
            notification.setActionUrl(actionUrl);
            notificationRepository.save(notification);

            firebaseService.pushNotificationToUser(notification, followedUser);
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
        User user = (User) authentication.getPrincipal();
        User currentUser = profileService.getUserWithProfile(user);
        if (!followRequest.getFollowed().getId().equals(currentUser.getId())) {
            return ResponseEntity.badRequest().body("You are not authorized to respond to this follow request.");
        }
        Optional<Follow> existingFollow = followRepository.findByFollowerIdAndFollowedId(followRequest.getFollower().getId(), currentUser.getId());
        if (existingFollow.isPresent()) {
            User followedUser = profileService.getUserWithProfile(userRepository.findById(followRequest.getFollower().getId())
                    .orElseThrow(() -> new RuntimeException("User not found")));
            if (responseStatus == FollowRequestStatus.PENDING) {
                return ResponseEntity.badRequest().body("Invalid response status.");
            } else if (responseStatus == FollowRequestStatus.ACCEPTED && followRequest.getStatus() == FollowRequestStatus.PENDING) {
                followRequest.setStatus(FollowRequestStatus.ACCEPTED);
                followRequestRepository.save(followRequest);

                Follow follow = new Follow();
                follow.setFollower(currentUser);
                follow.setFollowed(followedUser);
                followRepository.save(follow);

            } else if (responseStatus == FollowRequestStatus.REJECTED && followRequest.getStatus() == FollowRequestStatus.PENDING) {
                followRequest.setStatus(FollowRequestStatus.REJECTED);
                followRequestRepository.save(followRequest);
            }
            String message = currentUser.getUsername() + " has " + followRequest.getStatus().toString().toLowerCase(Locale.ROOT) + " your follow request.";
            String actionUrl = "/users/" + currentUser.getId();

            Notification notification = new Notification();
            notification.setSender(currentUser);
            notification.setReceiver(followedUser);
            notification.setType(NotificationType.FOLLOW_RESPONSE);
            notification.setMessage(message);
            notification.setActionUrl(actionUrl);
            notificationRepository.save(notification);

            firebaseService.pushNotificationToUser(notification, followedUser);
            return ResponseEntity.ok("Successfully " + followRequest.getStatus().toString().toLowerCase(Locale.ROOT) + " the follow request.");
        } else {
            return ResponseEntity.badRequest().body("The follower is not following you.");
        }
    }

    public ResponseEntity<?> getFollowRequests() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        List<FollowRequest> followRequests = followRequestRepository.findByFollowedIdAndStatus(currentUser.getId(), FollowRequestStatus.PENDING);
        List<FollowRequestResponse> followRequestResponses = followRequests.stream()
                .map(followRequest -> new FollowRequestResponse().toDTO(followRequest, profileResponseBuilder))
                .toList();
        return ResponseEntity.ok(followRequestResponses);
    }

    public ResponseEntity<?> getFollowers() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        List<Follow> followers = followRepository.findByFollowedId(currentUser.getId());
        List<User> followerUsers = followers.stream()
                .map(Follow::getFollower)
                .toList();
        List<UserResponse> followerUserResponses = new UserResponse().mapUsersToDTOs(followerUsers, profileResponseBuilder);
        return ResponseEntity.ok(followerUserResponses);
    }

    public ResponseEntity<?> getFollowing() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        List<Follow> following = followRepository.findByFollowerId(currentUser.getId());
        List<User> followingUsers = following.stream()
                .map(Follow::getFollowed)
                .toList();
        List<UserResponse> followingUserResponses = new UserResponse().mapUsersToDTOs(followingUsers, profileResponseBuilder);
        return ResponseEntity.ok(followingUserResponses);
    }
}
