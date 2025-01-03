package com.example.demo.services;

import com.example.demo.dtos.requests.CreatePostRequest;
import com.example.demo.dtos.requests.SharePostRequest;
import com.example.demo.dtos.requests.UpdatePostRequest;
import com.example.demo.dtos.responses.PostResponse;
import com.example.demo.enums.*;
import com.example.demo.models.*;
import com.example.demo.repositories.*;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.hibernate.Hibernate;
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
    private final SavedPostRepository savedPostRepository;
    private final NotificationService notificationService;
    private final NotificationRepository notificationRepository;
    private final ProfileRepository profileRepository;

    public List<PostResponse> getPostFeed(int page, int size) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
//        String followerId = currentUser.getId();
//        List<String> userIds = followRepository.findFollowedIdsByFollowerId(followerId);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        List<Post> posts = postRepository.findAll(pageable).getContent();
//        List<Post> posts = postRepository.findByUserIdsAndIsDeletedAndPrivacy(userIds, pageable).getContent();

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
        post.setLink(postRequest.getLink());
        Post savedPost = postRepository.save(post);

        if (mediaFiles != null && !mediaFiles.isEmpty()) {
            savedPost.setMediaFiles(mediaFileService.uploadMediaFile(savedPost.getId(), FeedItemType.POST, mediaFiles));
        }

        List<String> followerIds = followRepository.findFollowerIdsByFollowedId(currentUser.getId());
//        firebaseService.pushPostToReceivers(savedPost, followerIds);
        for (String followerId : followerIds) {
            User follower = userRepository.findById(followerId)
                    .orElseThrow(() -> new RuntimeException("Follower not found"));
            String title = currentUser.getUsername() + " created a new post";
            String message = savedPost.getTitle();
            Profile profile = profileRepository.findById(currentUser.getProfile().getId())
                    .orElseThrow(() -> new RuntimeException("Profile not found"));
            String avatar = profile.getProfileAvatar().getUrl();

            Notification notification = new Notification();
            notification.setSender(currentUser);
            notification.setReceiver(follower);
            notification.setType(NotificationType.POST);
            notification.setMessage(message);
            notification.setActionUrl("/posts/" + savedPost.getId());
            notificationRepository.save(notification);

            firebaseService.pushNotificationToUser(notification, follower);

            if (follower.getFcmToken() != null && !follower.getId().equals(currentUser.getId())) {
                Map<String, String> dataPayload = Map.of(
                        "type", NotificationType.POST.name(),
                        "postId", savedPost.getId().toString()
                );
                notificationService.sendNotification(follower.getFcmToken(), title, message, avatar, dataPayload);
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

    @Transactional
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

    @Transactional
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
            savedPost.setMediaFiles(mediaFileService.uploadMediaFile(savedPost.getId(), FeedItemType.POST, mediaFiles));
        }

        List<GroupMembership> adminMembers = groupMembershipRepository.findAdminsByGroupId(group.getId(), RoleName.ADMIN);
        List<String> memberIds = adminMembers.stream().map(member -> member.getUser().getId()).toList();

        List<User> groupAdmins = userRepository.findAllUsersByIdIn(memberIds);
        for (User member : groupAdmins) {
            if (!member.getId().equals(currentUser.getId())) {
                String title = "New post in " + group.getName() + " group";
                String message = currentUser.getUsername() + " created a new post in " + group.getName() + " group";
                Profile profile = profileRepository.findById(currentUser.getProfile().getId())
                        .orElseThrow(() -> new RuntimeException("Profile not found"));
                String avatar = profile.getProfileAvatar().getUrl();
                String actionUrl = "/group/" + group.getId() + "/posts/" + savedPost.getId();

                Notification notification = new Notification();
                notification.setSender(currentUser);
                notification.setReceiver(member);
                notification.setType(NotificationType.POST);
                notification.setMessage(message);
                notification.setActionUrl(actionUrl);
                notificationRepository.save(notification);

                firebaseService.pushNotificationToUser(notification, member);

                if (member.getFcmToken() != null && !member.getId().equals(currentUser.getId())) {
                    Map<String, String> dataPayload = Map.of(
                            "type", NotificationType.POST.name(),
                            "postId", savedPost.getId().toString()
                    );

                    notificationService.sendNotification(member.getFcmToken(), title, message, avatar, dataPayload);
                }
            }
        }
        return ResponseEntity.ok().body("Post created successfully");
    }

    @Transactional
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
//            firebaseService.pushPostToReceivers(post, memberIds);

            List<User> users = userRepository.findAllUsersByIdIn(memberIds);
            for (User member : users) {
                if (!member.getId().equals(post.getUser().getId())) {
                    String title = post.getUser().getUsername() + " created a new post in  " + group.getName() + " group";
                    String message = post.getTitle();
                    Profile profile = profileRepository.findById(post.getUser().getProfile().getId())
                            .orElseThrow(() -> new RuntimeException("Profile not found"));
                    String avatar = profile.getProfileAvatar().getUrl();
                    String actionUrl = "/posts/" + post.getId();

                    Notification notification = new Notification();
                    notification.setSender(post.getUser());
                    notification.setGroup(group);
                    notification.setReceiver(member);
                    notification.setType(NotificationType.POST);
                    notification.setMessage(message);
                    notification.setActionUrl(actionUrl);
                    notificationRepository.save(notification);
                    if (member.getFcmToken() != null) {
                        Map<String, String> dataPayload = Map.of(
                                "type", NotificationType.POST.name(),
                                "postId", post.getId().toString(),
                                "actionUrl", actionUrl,
                                "GroupId", group.getId().toString()
                        );
                        notificationService.sendNotification(member.getFcmToken(), title, message, avatar, dataPayload);
                    }

                } else {
                    String title = "Your post is approved";
                    String message = "Your post is approved in " + group.getName() + " group";
                    Profile profile = profileRepository.findById(currentUser.getProfile().getId())
                            .orElseThrow(() -> new RuntimeException("Profile not found"));
                    String avatar = profile.getProfileAvatar().getUrl();
                    String actionUrl = "/posts/" + post.getId();

                    Notification notification = new Notification();
                    notification.setSender(currentUser);
                    notification.setGroup(group);
                    notification.setReceiver(post.getUser());
                    notification.setType(NotificationType.POST);
                    notification.setMessage(message);
                    notification.setActionUrl(actionUrl);
                    notificationRepository.save(notification);

                    firebaseService.pushNotificationToUser(notification, member);

                    if (member.getFcmToken() != null) {
                        Map<String, String> dataPayload = Map.of(
                                "type", NotificationType.POST.name(),
                                "postId", post.getId().toString(),
                                "actionUrl", actionUrl,
                                "GroupId", group.getId().toString()
                        );
                        notificationService.sendNotification(post.getUser().getFcmToken(), title, message, avatar, dataPayload);
                    }
                }
            }
            post.setIsApproved(true);
        } else {
            post.setIsApproved(false);
            String title = "Your post is rejected";
            String message = "Your post is rejected from " + group.getName() + " group";
            Profile profile = profileRepository.findById(currentUser.getProfile().getId())
                    .orElseThrow(() -> new RuntimeException("Profile not found"));
            String avatar = profile.getProfileAvatar().getUrl();
            String actionUrl = "/posts/" + post.getId();

            Notification notification = new Notification();
            notification.setSender(currentUser);
            notification.setGroup(group);
            notification.setReceiver(post.getUser());
            notification.setType(NotificationType.POST);
            notification.setMessage(message);
            notification.setActionUrl(actionUrl);
            notificationRepository.save(notification);

            firebaseService.pushNotificationToUser(notification, post.getUser());

            if (post.getUser().getFcmToken() != null) {
                Map<String, String> dataPayload = Map.of(
                        "type", NotificationType.POST.name()
                );
                notificationService.sendNotification(post.getUser().getFcmToken(), title, message, avatar, dataPayload);
            }
        }
        postRepository.save(post);
        return ResponseEntity.ok().body("Post reviewed successfully");
    }

    public ResponseEntity<?> getPost(Long postId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        Post post = postRepository.findById(postId)
                .filter(p -> !p.getIsDeleted())
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

        List<PostReaction> reactions = postReactionRepository.findByUserIdAndPostIdIn(currentUser.getId(), posts.stream().map(Post::getId).collect(Collectors.toList()));
        Map<Long, ReactionTypeName> reactionTypeMap = new HashMap<>();
        for (PostReaction reaction : reactions) {
            reactionTypeMap.putIfAbsent(reaction.getPost().getId(), reaction.getReactionType().getName());
        }
        return new PostResponse().mapPostsToDTOs(posts, reactionTypeMap);
    }

    public ResponseEntity<?> getPostByUser(int page, int size) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        List<Post> posts = postRepository.findByUserId(currentUser.getId(), pageable);

        List<PostReaction> reactions = postReactionRepository.findByUserIdAndPostIdIn(currentUser.getId(), posts.stream().map(Post::getId).collect(Collectors.toList()));
        Map<Long, ReactionTypeName> reactionTypeMap = new HashMap<>();
        for (PostReaction reaction : reactions) {
            reactionTypeMap.putIfAbsent(reaction.getPost().getId(), reaction.getReactionType().getName());
        }

        return ResponseEntity.ok(new PostResponse().mapPostsToDTOs(posts, reactionTypeMap));
    }

    @Transactional
    public ResponseEntity<?> savePost(Long postId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        Post post = postRepository.findById(postId).orElseThrow(() -> new RuntimeException("Post not found"));
        if (post.getUser().getId().equals(currentUser.getId())) {
            return ResponseEntity.badRequest().body("You can't save your own post");
        }
        if (savedPostRepository.existsByUserIdAndPostId(currentUser.getId(), postId)) {
            return ResponseEntity.badRequest().body("Post is already saved");
        }
        SavedPost savedPost = new SavedPost();
        savedPost.setUser(currentUser);
        savedPost.setPost(post);
        savedPostRepository.save(savedPost);
        return ResponseEntity.ok().body("Post saved successfully");
    }

    @Transactional
    public ResponseEntity<?> unsavePost(Long postId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        Post post = postRepository.findById(postId).orElseThrow(() -> new RuntimeException("Post not found"));
        if (post.getUser().getId().equals(currentUser.getId())) {
            return ResponseEntity.badRequest().body("You can't unsave your own post");
        }
        if (!savedPostRepository.existsByUserIdAndPostId(currentUser.getId(), postId)) {
            return ResponseEntity.badRequest().body("Post is not saved");
        }
        savedPostRepository.deleteByUserIdAndPostId(currentUser.getId(), postId);
        return ResponseEntity.ok().body("Post unsaved successfully");
    }

    public ResponseEntity<?> getSavedPosts(int page, int size) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        Pageable pageable = PageRequest.of(page, size, Sort.by("savedAt").descending());
        List<SavedPost> savedPosts = savedPostRepository.findByUserId(currentUser.getId(), pageable);
        List<Post> posts = savedPosts.stream().map(SavedPost::getPost).collect(Collectors.toList());

        List<PostReaction> reactions = postReactionRepository.findByUserIdAndPostIdIn(currentUser.getId(), posts.stream().map(Post::getId).collect(Collectors.toList()));
        Map<Long, ReactionTypeName> reactionTypeMap = new HashMap<>();
        for (PostReaction reaction : reactions) {
            reactionTypeMap.putIfAbsent(reaction.getPost().getId(), reaction.getReactionType().getName());
        }

        return ResponseEntity.ok(new PostResponse().mapPostsToDTOs(posts, reactionTypeMap));
    }

    public ResponseEntity<?> sharePost(SharePostRequest sharePostRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        Post post = postRepository.findById(sharePostRequest.getOriginalPostId()).orElseThrow(() -> new RuntimeException("Post not found"));
        Privacy privacy = privacyRepository.findById(sharePostRequest.getPrivacyId()).orElseThrow(() -> new RuntimeException("Privacy not found"));
        if (post.getUser().getId().equals(currentUser.getId())) {
            return ResponseEntity.badRequest().body("You can't share your own post");
        }
        Post sharedPost = new Post();
        sharedPost.setTitle(sharePostRequest.getTitle());
        sharedPost.setTextContent(sharePostRequest.getAdditionalContent());
        sharedPost.setUser(currentUser);
        sharedPost.setPrivacy(privacy);
        sharedPost.setIsShared(true);
        sharedPost.setSharedPost(post);
        postRepository.save(sharedPost);
        return ResponseEntity.ok().body("Post shared successfully");
    }


//    public ResponseEntity<?> createImage(List<MultipartFile> files) {
//        return ResponseEntity.ok(mediaFileService.uploadMediaFile(files));
//    }

}
