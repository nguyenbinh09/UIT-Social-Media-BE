package com.example.demo.services;

import com.example.demo.dtos.requests.CreatePostRequest;
import com.example.demo.models.Post;
import com.example.demo.models.User;
import com.example.demo.repositories.FollowRepository;
import com.example.demo.repositories.PostReposiroty;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;

@Service
@AllArgsConstructor
public class PostService {
    private final List<Post> newPostsBuffer = new LinkedList<>();
    private final PostReposiroty postReposiroty;
    private final FirebaseService firebaseService;
    private final FollowRepository followRepository;

    public List<Post> getPostFeed(int page, int size) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        String followerId = currentUser.getId();
        List<String> userIds = followRepository.findFollowedIdsByFollowerId(followerId);
        System.out.println(userIds);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        List<Post> posts = postReposiroty.findByUserIdIn(userIds, pageable).getContent();
        System.out.println(posts);
        return posts;
    }


    public ResponseEntity<?> createPost(CreatePostRequest postRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        Post post = new Post();
        post.setTitle(postRequest.getTitle());
        post.setTextContent(postRequest.getTextContent());
        post.setUser(currentUser);
        postReposiroty.save(post);

        List<String> followerIds = followRepository.findFollowerIdsByFollowedId(postRequest.getUserId());
        firebaseService.pushPostToFollowers(post, followerIds);
        return ResponseEntity.ok().body("Post created successfully");
    }
}
