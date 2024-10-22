package com.example.demo.services;

import com.example.demo.dtos.requests.CreatePostRequest;
import com.example.demo.dtos.responses.PostResponse;
import com.example.demo.models.Post;
import com.example.demo.models.Privacy;
import com.example.demo.models.User;
import com.example.demo.repositories.FollowRepository;
import com.example.demo.repositories.PostReposiroty;
import com.example.demo.repositories.PrivacyRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.LinkedList;
import java.util.List;

@Service
@AllArgsConstructor
public class PostService {
    private final List<Post> newPostsBuffer = new LinkedList<>();
    private final PostReposiroty postReposiroty;
    private final FirebaseService firebaseService;
    private final FollowRepository followRepository;
    private final PrivacyRepository privacyRepository;
    private final MediaFileService mediaFileService;

    public List<PostResponse> getPostFeed(int page, int size) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        String followerId = currentUser.getId();
        List<String> userIds = followRepository.findFollowedIdsByFollowerId(followerId);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        List<Post> posts = postReposiroty.findByUserIdIn(userIds, pageable).getContent();

        return new PostResponse().mapPostsToDTOs(posts);
    }

    @Transactional
    public ResponseEntity<?> createPost(CreatePostRequest postRequest, List<MultipartFile> mediaFiles) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        Privacy privacy = privacyRepository.findById(postRequest.getPrivacyId()).orElseThrow(() -> new RuntimeException("Privacy not found"));

        Post post = new Post();
        post.setTitle(postRequest.getTitle());
        post.setTextContent(postRequest.getTextContent());
        post.setUser(currentUser);
        post.setPrivacy(privacy);


        post.setMediaFiles(mediaFileService.uploadMediaFile(post, mediaFiles));
        postReposiroty.save(post);

        List<String> followerIds = followRepository.findFollowerIdsByFollowedId(currentUser.getId());
        firebaseService.pushPostToFollowers(post, followerIds);
        return ResponseEntity.ok().body("Post created successfully");
    }

//    public ResponseEntity<?> createImage(List<MultipartFile> files) {
//        return ResponseEntity.ok(mediaFileService.uploadMediaFile(files));
//    }
}
