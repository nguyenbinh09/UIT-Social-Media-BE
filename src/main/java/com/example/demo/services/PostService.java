package com.example.demo.services;

import com.example.demo.dtos.requests.CreatePostRequest;
import com.example.demo.dtos.requests.UpdatePostRequest;
import com.example.demo.dtos.responses.PostResponse;
import com.example.demo.enums.FeedItemType;
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
        if(mediaFiles != null && !mediaFiles.isEmpty()) {
            post.setMediaFiles(mediaFileService.uploadMediaFile(post.getId(), FeedItemType.POST, mediaFiles));
        }
        postReposiroty.save(post);

        List<String> followerIds = followRepository.findFollowerIdsByFollowedId(currentUser.getId());
        firebaseService.pushPostToFollowers(post, followerIds);
        return ResponseEntity.ok().body("Post created successfully");
    }

    @Transactional
    public ResponseEntity<?> updatePost(UpdatePostRequest postRequest, List<MultipartFile> mediaFiles) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        Post post = postReposiroty.findById(postRequest.getId()).orElseThrow(() -> new RuntimeException("Post not found"));
        if(!post.getUser().getId().equals(currentUser.getId())) {
            return ResponseEntity.badRequest().body("You are not authorized to update this post");
        }
        if (postRequest.getTitle() != null && !post.getTitle().equals(postRequest.getTitle())) {
            post.setTitle(postRequest.getTitle());
        }
        if (postRequest.getTextContent() != null && !post.getTextContent().equals(postRequest.getTextContent())) {
            post.setTextContent(postRequest.getTextContent());
        }
        if (postRequest.getPrivacyId() != null && !post.getPrivacy().getId().equals(postRequest.getPrivacyId())) {
            Privacy privacy = privacyRepository.findById(postRequest.getPrivacyId()).orElseThrow(() -> new RuntimeException("Privacy not found"));
            post.setPrivacy(privacy);
        }
        if (mediaFiles != null && !mediaFiles.isEmpty()) {
            mediaFileService.deleteMediaFiles(post.getMediaFiles());
            post.setMediaFiles(mediaFileService.uploadMediaFile(post.getId(), FeedItemType.POST, mediaFiles));
        }
        return ResponseEntity.ok().body("Post updated successfully");
    }

    public ResponseEntity<?> deletePost(Long postId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        Post post = postReposiroty.findById(postId).orElseThrow(() -> new RuntimeException("Post not found"));
        if(!post.getUser().getId().equals(currentUser.getId())) {
            return ResponseEntity.badRequest().body("You are not authorized to delete this post");
        }
        postReposiroty.delete(post);
        return ResponseEntity.ok().body("Post deleted successfully");
    }

//    public ResponseEntity<?> createImage(List<MultipartFile> files) {
//        return ResponseEntity.ok(mediaFileService.uploadMediaFile(files));
//    }
}
