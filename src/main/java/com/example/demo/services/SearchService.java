package com.example.demo.services;

import com.example.demo.dtos.responses.GroupResponse;
import com.example.demo.dtos.responses.PostResponse;
import com.example.demo.dtos.responses.UserResponse;
import com.example.demo.models.User;
import com.example.demo.repositories.SearchRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class SearchService {

    private final SearchRepository searchRepository;

    public Map<String, List<?>> searchAll(String keyword) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        List<PostResponse> postResponses = searchRepository.searchPosts(keyword, currentUser);
        List<UserResponse> userResponses = searchRepository.searchUsers(keyword);
        List<GroupResponse> groupResponses = searchRepository.searchGroups(keyword);

        Map<String, List<?>> results = new HashMap<>();
        results.put("posts", postResponses);
        results.put("users", userResponses);
        results.put("groups", groupResponses);
        return results;
    }

    public List<PostResponse> searchPosts(String keyword) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        return searchRepository.searchPosts(keyword, currentUser);
    }

    public List<UserResponse> searchUsers(String keyword) {
        return searchRepository.searchUsers(keyword);
    }

    public List<GroupResponse> searchGroups(String keyword) {
        return searchRepository.searchGroups(keyword);
    }
}