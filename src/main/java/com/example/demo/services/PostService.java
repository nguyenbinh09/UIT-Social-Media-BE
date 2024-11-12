package com.example.demo.services;

import com.example.demo.dtos.requests.CreatePostRequest;
import com.example.demo.dtos.requests.UpdatePostRequest;
import com.example.demo.dtos.responses.PostResponse;
import com.example.demo.enums.*;
import com.example.demo.models.*;
import com.example.demo.repositories.*;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class PostService {
    private final List<Post> newPostsBuffer = new LinkedList<>();
    private final PostRepository postRepository;
    private final FirebaseService firebaseService;
    private final FollowRepository followRepository;
    private final PrivacyRepository privacyRepository;
    private final MediaFileService mediaFileService;
    private final UserRepository userRepository;
    private final PostReactionRepository postReactionRepository;
    private final GroupRepository groupRepository;
    private final GroupMembershipRepository groupMembershipRepository;

    public List<PostResponse> getPostFeed(int page, int size) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        String followerId = currentUser.getId();
        List<String> userIds = followRepository.findFollowedIdsByFollowerId(followerId);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        List<Post> posts = postRepository.findByUserIdsAndIsDeletedAndPrivacy(userIds, pageable).getContent();

        List<PostReaction> reactions = postReactionRepository.findByUserIdAndPostIdIn(currentUser.getId(), posts.stream().map(Post::getId).collect(Collectors.toList()));
        Map<Long, ReactionTypeName> reactionTypeMap = new HashMap<>();
        for (PostReaction reaction : reactions) {
            reactionTypeMap.putIfAbsent(reaction.getPost().getId(), reaction.getReactionType().getName());
        }
        return new PostResponse().mapPostsToDTOs(posts, reactionTypeMap);
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
        Post savedPost = postRepository.save(post);

        if (mediaFiles != null && !mediaFiles.isEmpty()) {
            post.setMediaFiles(mediaFileService.uploadMediaFile(savedPost.getId(), FeedItemType.POST, mediaFiles));
        }

        List<String> followerIds = followRepository.findFollowerIdsByFollowedId(currentUser.getId());
        firebaseService.pushPostToReceivers(savedPost, followerIds);

        for (String followerId : followerIds) {
            User follower = userRepository.findById(followerId)
                    .orElseThrow(() -> new RuntimeException("Follower not found"));

            if (follower.getFcmToken() != null && !follower.getId().equals(currentUser.getId())) {
                String title = currentUser.getUsername() + " created a new post";
                String message = savedPost.getTitle();

                firebaseService.sendNotificationToUser(follower.getFcmToken(), title, message);
            }
        }
        return ResponseEntity.ok().body("Post created successfully");
    }

    @Transactional
    public ResponseEntity<?> updatePost(UpdatePostRequest postRequest, List<MultipartFile> mediaFiles) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        Post post = postRepository.findById(postRequest.getId()).orElseThrow(() -> new RuntimeException("Post not found"));
        if (!post.getUser().getId().equals(currentUser.getId())) {
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
        Post post = postRepository.findById(postId).orElseThrow(() -> new RuntimeException("Post not found"));
        if (!post.getUser().getId().equals(currentUser.getId())) {
            return ResponseEntity.badRequest().body("You are not authorized to delete this post");
        }
        post.setIsDeleted(true);
        postRepository.save(post);
        return ResponseEntity.ok().body("Post deleted successfully");
    }

    public ResponseEntity<?> createGroupPost(Long groupId, CreatePostRequest postRequest, List<MultipartFile> mediaFiles) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        Privacy privacy = privacyRepository.findById(postRequest.getPrivacyId()).orElseThrow(() -> new RuntimeException("Privacy not found"));
        Group group = groupRepository.findById(groupId).orElseThrow(() -> new RuntimeException("Group not found"));
        groupMembershipRepository.findByUserIdAndGroupIdAndIsDeleted(currentUser.getId(), groupId, false)
                .orElseThrow(() -> new RuntimeException("You are not a member of this group"));

        Post post = new Post();
        post.setTitle(postRequest.getTitle());
        post.setTextContent(postRequest.getTextContent());
        post.setUser(currentUser);
        post.setPrivacy(privacy);
        post.setGroup(group);
        Post savedPost = postRepository.save(post);

        if (mediaFiles != null && !mediaFiles.isEmpty()) {
            post.setMediaFiles(mediaFileService.uploadMediaFile(savedPost.getId(), FeedItemType.POST, mediaFiles));
        }

        List<GroupMembership> adminMembers = groupMembershipRepository.findAdminsByGroupId(group.getId());
        List<String> memberIds = adminMembers.stream().map(member -> member.getUser().getId()).toList();

        List<User> groupAdmins = userRepository.findAllUsersByIdIn(memberIds);
        for (User member : groupAdmins) {
            if (member.getFcmToken() != null && !member.getId().equals(currentUser.getId())) {
                String title = currentUser.getUsername() + " created a new post in " + group.getName() + " group";
                String message = savedPost.getTitle();

//                firebaseService.sendNotificationToUser(member.getFcmToken(), title, message);
            }
        }
        return ResponseEntity.ok().body("Post created successfully");
    }

    public ResponseEntity<?> reviewPostInGroup(Long postId, boolean isApproved) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        Post post = postRepository.findById(postId).orElseThrow(() -> new RuntimeException("Post not found"));
        if (post.getIsDeleted().equals(true)) {
            return ResponseEntity.badRequest().body("Post is deleted");
        }
        Group group = groupRepository.findById(post.getGroup().getId()).orElseThrow(() -> new RuntimeException("Group not found"));
        if (group.getIsDeleted().equals(true)) {
            return ResponseEntity.badRequest().body("Group is deleted");
        }
        GroupMembership groupMembership = groupMembershipRepository.findByUserIdAndGroupIdAndIsDeleted(currentUser.getId(), group.getId(), false)
                .orElseThrow(() -> new RuntimeException("You are not a member of this group"));
        if (!groupMembership.getRole().equals(RoleName.ADMIN)) {
            return ResponseEntity.badRequest().body("You don't have permission to review posts in this group");
        }
        if (isApproved) {
            List<GroupMembership> members = groupMembershipRepository.findAllByGroupId(group.getId());
            List<String> memberIds = members.stream().map(member -> member.getUser().getId()).toList();
            firebaseService.pushPostToReceivers(post, memberIds);

            List<User> users = userRepository.findAllUsersByIdIn(memberIds);
            for (User member : users) {
                if (member.getFcmToken() != null) {
                    if (!member.getId().equals(post.getUser().getId())) {
                        String title = post.getUser().getUsername() + " created a new post in  " + group.getName() + " group";
                        String message = post.getTitle();

//                        firebaseService.sendNotificationToUser(member.getFcmToken(), title, message);
                    } else {
//                        firebaseService.sendNotificationToUser(post.getUser().getFcmToken(), "Post is approved", "Your post is approved");
                    }
                }
            }
            post.setIsApproved(true);
        } else {
            post.setIsApproved(false);
//            firebaseService.sendNotificationToUser(post.getUser().getFcmToken(), "Post is rejected", "Your post is rejected");
        }
        postRepository.save(post);
        return ResponseEntity.ok().body("Post reviewed successfully");
    }

    public ResponseEntity<?> getPost(Long postId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        Post post = postRepository.findById(postId)
                .filter(p -> !p.getIsDeleted()) // Filter out deleted posts
                .orElseThrow(() -> new RuntimeException("Post not found or has been deleted"));

        Optional<PostReaction> postReaction = postReactionRepository.findByPostIdAndUserId(post.getId(), currentUser.getId());

        PrivacyName privacy = post.getPrivacy().getName();

        if (privacy.equals(PrivacyName.PUBLIC) || currentUser.getId().equals(post.getUser().getId())) {
            return postReaction.map(reaction -> ResponseEntity.ok(new PostResponse().toDTOWithReaction(post, reaction.getReactionType().getName())))
                    .orElseGet(() -> ResponseEntity.ok(new PostResponse().toDTO(post)));
        }

        if (privacy.equals(PrivacyName.PRIVATE)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You don't have permission to view this private post.");
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Post access is restricted.");
    }

    public List<PostResponse> getGroupPosts(Long groupId, int page, int size) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        groupRepository.findById(groupId).orElseThrow(() -> new RuntimeException("Group not found"));
        groupMembershipRepository.findByUserIdAndGroupIdAndIsDeleted(currentUser.getId(), groupId, false)
                .orElseThrow(() -> new RuntimeException("You are not a member of this group"));

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        List<Post> posts = postRepository.findByGroupIdAndIsDeleted(groupId, pageable);
        for (Post post : posts) {
            System.out.println(post.getId());
        }

        List<PostReaction> reactions = postReactionRepository.findByUserIdAndPostIdIn(currentUser.getId(), posts.stream().map(Post::getId).collect(Collectors.toList()));
        Map<Long, ReactionTypeName> reactionTypeMap = new HashMap<>();
        for (PostReaction reaction : reactions) {
            reactionTypeMap.putIfAbsent(reaction.getPost().getId(), reaction.getReactionType().getName());
        }
        return new PostResponse().mapPostsToDTOs(posts, reactionTypeMap);
    }


//    public ResponseEntity<?> createImage(List<MultipartFile> files) {
//        return ResponseEntity.ok(mediaFileService.uploadMediaFile(files));
//    }

}
