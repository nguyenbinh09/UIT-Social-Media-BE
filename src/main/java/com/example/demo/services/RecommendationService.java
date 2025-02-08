package com.example.demo.services;

import com.example.demo.dtos.responses.UserResponse;
import com.example.demo.models.User;
import com.example.demo.repositories.FollowRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class RecommendationService {
    private final FollowRepository followRepository;
    private final ProfileResponseBuilder profileResponseBuilder;

    public ResponseEntity<?> recommendUsers() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        List<User> friendsOfFriends = followRepository.findFriendsOfFriends(currentUser.getId());

//        List<User> popularUsers = userRepository.findPopularUsersExcluding(currentUser.getId());
//
//        List<User> sharedInterestUsers = userRepository.findUsersWithSharedInterests(currentUser.getId());
        List<UserResponse> friendsOfFriendsResponse = new UserResponse().mapUsersToDTOs(friendsOfFriends, profileResponseBuilder);
        Set<UserResponse> recommendedUsers = new LinkedHashSet<>();
        recommendedUsers.addAll(friendsOfFriendsResponse);
//        recommendedUsers.addAll(sharedInterestUsers);
//        recommendedUsers.addAll(popularUsers);
        return ResponseEntity.ok(recommendedUsers);
    }
}
