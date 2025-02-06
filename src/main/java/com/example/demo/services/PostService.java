package com.example.demo.services;

import com.example.demo.dtos.requests.CreatePostRequest;
import com.example.demo.dtos.requests.SharePostRequest;
import com.example.demo.dtos.requests.UpdatePostRequest;
import com.example.demo.dtos.responses.ModerationResponse;
import com.example.demo.dtos.responses.PendingPostResponse;
import com.example.demo.dtos.responses.PostResponse;
import com.example.demo.enums.*;
import com.example.demo.models.*;
import com.example.demo.repositories.*;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
//@AllArgsConstructor
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
    private final ProfileService profileService;
    private final ProfileResponseBuilder profileResponseBuilder;
    private final RestTemplate restTemplate;
    private final TopicRepository topicRepository;

    public PostService(PostRepository postRepository,
                       FirebaseService firebaseService,
                       FollowRepository followRepository,
                       PrivacyRepository privacyRepository,
                       MediaFileService mediaFileService,
                       UserRepository userRepository,
                       PostReactionRepository postReactionRepository,
                       GroupRepository groupRepository,
                       GroupMembershipRepository groupMembershipRepository,
                       SavedPostRepository savedPostRepository,
                       NotificationService notificationService,
                       NotificationRepository notificationRepository,
                       ProfileService profileService,
                       ProfileResponseBuilder profileResponseBuilder,
                       RestTemplate restTemplate,
                       TopicRepository topicRepository) {
        this.postRepository = postRepository;
        this.firebaseService = firebaseService;
        this.followRepository = followRepository;
        this.privacyRepository = privacyRepository;
        this.mediaFileService = mediaFileService;
        this.userRepository = userRepository;
        this.postReactionRepository = postReactionRepository;
        this.groupRepository = groupRepository;
        this.groupMembershipRepository = groupMembershipRepository;
        this.savedPostRepository = savedPostRepository;
        this.notificationService = notificationService;
        this.notificationRepository = notificationRepository;
        this.profileService = profileService;
        this.profileResponseBuilder = profileResponseBuilder;
        this.restTemplate = restTemplate;
        this.topicRepository = topicRepository;
    }

    @Value("${uit-model.url}")
    private String modelUrl;

    public List<PostResponse> getPostFeed(int page, int size) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
//        String followerId = currentUser.getId();
//        List<String> userIds = followRepository.findFollowedIdsByFollowerId(followerId);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        List<Post> posts = postRepository.findAllWithStatus(pageable);
//        List<Post> posts = postRepository.findByUserIdsAndIsDeletedAndPrivacy(userIds, pageable).getContent();

        List<Long> savedPostIds = savedPostRepository.findPostIdsByUserId(currentUser.getId());

        List<PostReaction> reactions = postReactionRepository.findByUserIdAndPostIdIn(currentUser.getId(), posts.stream().map(Post::getId).collect(Collectors.toList()));
        Map<Long, ReactionTypeName> reactionTypeMap = new HashMap<>();
        for (PostReaction reaction : reactions) {
            reactionTypeMap.putIfAbsent(reaction.getPost().getId(), reaction.getReactionType().getName());
        }
        return new PostResponse().mapPostsToDTOs(posts, reactionTypeMap, savedPostIds, profileResponseBuilder);
    }

    @Transactional
    public ResponseEntity<?> createPost(CreatePostRequest postRequest, List<MultipartFile> mediaFiles) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();

        User currentUser = profileService.getUserWithProfile(user);
        Privacy privacy = privacyRepository.findById(postRequest.getPrivacyId()).orElseThrow(() -> new RuntimeException("Privacy not found"));
        if (postRequest.getTopicIds().size() > 3) {
            return ResponseEntity.badRequest().body("You can't select more than 3 topics");
        }
        List<Topic> topics = topicRepository.findAllById(postRequest.getTopicIds());
        if (topics.isEmpty()) {
            return ResponseEntity.badRequest().body("Invalid topics");
        }
        String apiUrl = modelUrl + "/predict";
        Map<String, String> payload = Map.of("text", postRequest.getTextContent());
        ResponseEntity<ModerationResponse> response = restTemplate.postForEntity(
                apiUrl,
                payload,
                ModerationResponse.class
        );

        ModerationResponse responseBody = response.getBody();
        boolean isClean;
        if (response.getStatusCode().is2xxSuccessful() && responseBody != null) {
            isClean = responseBody.getPredictions().stream().allMatch(pred -> pred.equalsIgnoreCase("Clean"));
        } else {
            isClean = false;
        }
        Post post = new Post();
        post.setTitle(postRequest.getTitle());
        post.setTextContent(postRequest.getTextContent());
        post.setUser(currentUser);
        post.setPrivacy(privacy);
        post.setTopics(topics);
        post.setLink(postRequest.getLink());
        post.setStatus(isClean ? PostStatus.APPROVED : PostStatus.PENDING);
        Post savedPost = postRepository.save(post);

        if (mediaFiles != null && !mediaFiles.isEmpty()) {
            savedPost.setMediaFiles(mediaFileService.uploadMediaFile(savedPost.getId(), FeedItemType.POST, mediaFiles));
        }
        if (!isClean) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Your post has been flagged for moderation. It will be reviewed by an admin before being published.");
        } else {
            List<String> followerIds = followRepository.findFollowerIdsByFollowedId(currentUser.getId());
//        firebaseService.pushPostToReceivers(savedPost, followerIds);
            for (String followerId : followerIds) {
                User follower = profileService.getUserWithProfile(userRepository.findById(followerId).orElseThrow(() -> new RuntimeException("Follower not found")));
                String title = currentUser.getUsername() + " created a new post";
                String message = "Check out " + currentUser.getUsername() + "'s new post: " + savedPost.getTextContent();
                ;
                Profile profile = profileService.getProfileByUser(currentUser);
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
                    notificationService.sendNotification(follower, title, message, avatar, dataPayload);
                }
            }
            PostResponse postResponse = new PostResponse().toDTO(savedPost, profileResponseBuilder);
            return ResponseEntity.ok().body(postResponse);
        }
    }

    @Transactional
    public ResponseEntity<?> updatePost(UpdatePostRequest postRequest, List<MultipartFile> mediaFiles) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        Post post = postRepository.findById(postRequest.getId()).orElseThrow(() -> new RuntimeException("Post not found"));
        updatePost(post, postRequest, mediaFiles, currentUser);
        Post savedPost = postRepository.save(post);
        PostResponse postResponse = new PostResponse().toDTO(savedPost, profileResponseBuilder);
        return ResponseEntity.ok().body(postResponse);
    }

    public void updatePost(Post post, UpdatePostRequest postRequest, List<MultipartFile> mediaFiles, User currentUser) {
        if (!post.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("You are not authorized to update this post");
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
        Post savedPost = postRepository.save(post);
        PostResponse postResponse = new PostResponse().toDTO(savedPost, profileResponseBuilder);
        return ResponseEntity.ok().body(postResponse);
    }

    @Transactional
    public ResponseEntity<?> createGroupPost(Long groupId, CreatePostRequest postRequest, List<MultipartFile> mediaFiles) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        User currentUser = profileService.getUserWithProfile(user);
        Privacy privacy = privacyRepository.findById(postRequest.getPrivacyId()).orElseThrow(() -> new RuntimeException("Privacy not found"));
        Group group = groupRepository.findById(groupId).orElseThrow(() -> new RuntimeException("Group not found"));
        GroupMembership groupMembership = groupMembershipRepository.findByUserIdAndGroupId(currentUser.getId(), groupId)
                .orElseThrow(() -> new RuntimeException("You are not a member of this group"));
        Post post = new Post();
        post.setTitle(postRequest.getTitle());
        post.setTextContent(postRequest.getTextContent());
        post.setUser(currentUser);
        post.setPrivacy(privacy);
        post.setGroup(group);
        post.setLink(postRequest.getLink());
        if (groupMembership.getRole().equals(RoleName.ADMIN)) {
            post.setStatus(PostStatus.APPROVED);
        } else {
            post.setStatus(PostStatus.PENDING);
        }
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
                Profile profile = profileService.getProfileByRole(currentUser);
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

                    notificationService.sendNotification(member, title, message, avatar, dataPayload);
                }
            }
        }
        return ResponseEntity.ok().body(new PostResponse().toDTO(savedPost, profileResponseBuilder));
    }

    @Transactional
    public ResponseEntity<?> reviewPostInGroup(Long postId, boolean isApproved) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        User currentUser = profileService.getUserWithProfile(user);
        Post post = postRepository.findById(postId).orElseThrow(() -> new RuntimeException("Post not found"));
        if (!post.getStatus().equals(PostStatus.PENDING)) {
            return ResponseEntity.badRequest().body("Post is already approved");
        }
        User postOwner = profileService.getUserWithProfile(post.getUser());
        if (post.getIsDeleted().equals(true)) {
            return ResponseEntity.badRequest().body("Post is deleted");
        }
        Group group = groupRepository.findById(post.getGroup().getId()).orElseThrow(() -> new RuntimeException("Group not found"));
        if (group.getIsDeleted().equals(true)) {
            return ResponseEntity.badRequest().body("Group is deleted");
        }
        GroupMembership groupMembership = groupMembershipRepository.findByUserIdAndGroupId(currentUser.getId(), group.getId())
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
                if (!member.getId().equals(postOwner.getId())) {
                    String title = postOwner.getUsername() + " created a new post in  " + group.getName() + " group";
                    String message = post.getTitle();
                    Profile profile = profileService.getProfileByUser(profileService.getUserWithProfile(post.getUser()));
                    String avatar = profile.getProfileAvatar().getUrl();
                    String actionUrl = "/posts/" + post.getId();

                    Notification notification = new Notification();
                    notification.setSender(postOwner);
                    notification.setGroup(group);
                    notification.setReceiver(member);
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
                        notificationService.sendNotification(member, title, message, avatar, dataPayload);
                    }

                } else {
                    String title = "Your post is approved";
                    String message = "Your post is approved in " + group.getName() + " group";
                    Profile profile = profileService.getProfileByUser(currentUser);
                    String avatar = profile.getProfileAvatar().getUrl();
                    String actionUrl = "/posts/" + post.getId();

                    Notification notification = new Notification();
                    notification.setSender(currentUser);
                    notification.setGroup(group);
                    notification.setReceiver(postOwner);
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
                        notificationService.sendNotification(post.getUser(), title, message, avatar, dataPayload);
                    }
                }
            }
            post.setStatus(PostStatus.APPROVED);
        } else {
            post.setStatus(PostStatus.REJECTED);
            String title = "Your post is rejected";
            String message = "Your post is rejected from " + group.getName() + " group";
            Profile profile = profileService.getProfileByUser(currentUser);
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
                notificationService.sendNotification(post.getUser(), title, message, avatar, dataPayload);
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
            return postReaction.map(reaction -> ResponseEntity.ok(new PostResponse().toDTOWithReaction(post, reaction.getReactionType().getName(), profileResponseBuilder)))
                    .orElseGet(() -> ResponseEntity.ok(new PostResponse().toDTO(post, profileResponseBuilder)));
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
        groupMembershipRepository.findByUserIdAndGroupId(currentUser.getId(), groupId)
                .orElseThrow(() -> new RuntimeException("You are not a member of this group"));

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        List<Post> posts = postRepository.findByGroupIdAndIsDeleted(groupId, pageable);

        List<Long> savedPostIds = savedPostRepository.findPostIdsByUserId(currentUser.getId());

        List<PostReaction> reactions = postReactionRepository.findByUserIdAndPostIdIn(currentUser.getId(), posts.stream().map(Post::getId).collect(Collectors.toList()));
        Map<Long, ReactionTypeName> reactionTypeMap = new HashMap<>();
        for (PostReaction reaction : reactions) {
            reactionTypeMap.putIfAbsent(reaction.getPost().getId(), reaction.getReactionType().getName());
        }
        return new PostResponse().mapPostsToDTOs(posts, reactionTypeMap, savedPostIds, profileResponseBuilder);
    }

    public ResponseEntity<?> getPostByUser(int page, int size) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        List<Post> posts = postRepository.findByCurrentUserId(currentUser.getId(), pageable);

        List<Long> savedPostIds = savedPostRepository.findPostIdsByUserId(currentUser.getId());

        List<PostReaction> reactions = postReactionRepository.findByUserIdAndPostIdIn(currentUser.getId(), posts.stream().map(Post::getId).collect(Collectors.toList()));
        Map<Long, ReactionTypeName> reactionTypeMap = new HashMap<>();
        for (PostReaction reaction : reactions) {
            reactionTypeMap.putIfAbsent(reaction.getPost().getId(), reaction.getReactionType().getName());
        }

        return ResponseEntity.ok(new PostResponse().mapPostsToDTOs(posts, reactionTypeMap, savedPostIds, profileResponseBuilder));
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

        List<Long> savedPostIds = savedPostRepository.findPostIdsByUserId(currentUser.getId());

        List<PostReaction> reactions = postReactionRepository.findByUserIdAndPostIdIn(currentUser.getId(), posts.stream().map(Post::getId).collect(Collectors.toList()));
        Map<Long, ReactionTypeName> reactionTypeMap = new HashMap<>();
        for (PostReaction reaction : reactions) {
            reactionTypeMap.putIfAbsent(reaction.getPost().getId(), reaction.getReactionType().getName());
        }

        return ResponseEntity.ok(new PostResponse().mapPostsToDTOs(posts, reactionTypeMap, savedPostIds, profileResponseBuilder));
    }

    @Transactional
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

    public ResponseEntity<?> getPostByUserId(String userId, int page, int size) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        List<Post> posts = postRepository.findByUserId(user.getId(), pageable);

        List<Long> savedPostIds = savedPostRepository.findPostIdsByUserId(user.getId());

        List<PostReaction> reactions = postReactionRepository.findByUserIdAndPostIdIn(user.getId(), posts.stream().map(Post::getId).collect(Collectors.toList()));
        Map<Long, ReactionTypeName> reactionTypeMap = new HashMap<>();
        for (PostReaction reaction : reactions) {
            reactionTypeMap.putIfAbsent(reaction.getPost().getId(), reaction.getReactionType().getName());
        }

        return ResponseEntity.ok(new PostResponse().mapPostsToDTOs(posts, reactionTypeMap, savedPostIds, profileResponseBuilder));
    }

    public ResponseEntity<?> getPendingPosts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").ascending());
        List<Post> pendingPosts = postRepository.findByStatusWithoutGroupId(PostStatus.PENDING, pageable);
        List<PendingPostResponse> postResponses = new PendingPostResponse().mapPostsToDTOs(pendingPosts, profileResponseBuilder);
        return ResponseEntity.ok(postResponses);
    }

    public ResponseEntity<?> getPendingGroupPosts(Long groupId, int page, int size) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        Group group = groupRepository.findById(groupId).orElseThrow(() -> new RuntimeException("Group not found"));
        GroupMembership groupMembership = groupMembershipRepository.findByUserIdAndGroupId(currentUser.getId(), groupId)
                .orElseThrow(() -> new RuntimeException("You are not a member of this group"));
        if (!groupMembership.getRole().equals(RoleName.ADMIN)) {
            return ResponseEntity.badRequest().body("You don't have permission to view pending posts in this group");
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").ascending());
        List<Post> pendingPosts = postRepository.findByStatusAndGroupId(PostStatus.PENDING, group.getId(), pageable);
        List<PendingPostResponse> postResponses = new PendingPostResponse().mapPostsToDTOs(pendingPosts, profileResponseBuilder);
        return ResponseEntity.ok(postResponses);
    }

    @Transactional
    public ResponseEntity<?> approvePost(Long postId, PostStatus status) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        Post post = postRepository.findByIdWithoutGroupId(postId).orElseThrow(() -> new RuntimeException("Post not found"));
        if (status.equals(PostStatus.PENDING) || status.equals(PostStatus.REJECTED)) {
            return ResponseEntity.badRequest().body("Invalid post status");
        } else if (post.getStatus().equals(PostStatus.APPROVED)) {
            return ResponseEntity.badRequest().body("Post is already approved");
        }
        post.setStatus(status);
        post.setReviewedBy(currentUser);
        Post savedPost = postRepository.save(post);
        User postOwner = profileService.getUserWithProfile(savedPost.getUser());

        String postOwnerTitle = "Amin approved your post";
        String postOwnerMessage = "Your post has been approved. It is now visible to other users.";
        String adminAvatar = "https://firebasestorage.googleapis.com/v0/b/uit-social-network-f592d.appspot.com/o/admin-avatar.jpg?alt=media&token=91551f76-4094-42de-b3c4-2ceb0622e812";

        Notification postOwnernotification = new Notification();
        postOwnernotification.setSender(currentUser);
        postOwnernotification.setReceiver(postOwner);
        postOwnernotification.setType(NotificationType.POST_APPROVAL);
        postOwnernotification.setMessage(postOwnerMessage);
        postOwnernotification.setActionUrl("/posts/" + savedPost.getId());
        notificationRepository.save(postOwnernotification);

        firebaseService.pushNotificationToUser(postOwnernotification, postOwner);

        if (postOwner.getFcmToken() != null) {
            Map<String, String> dataPayload = Map.of(
                    "type", NotificationType.POST_APPROVAL.name(),
                    "postId", savedPost.getId().toString(),
                    "actionUrl", postOwnernotification.getActionUrl()
            );
            notificationService.sendNotification(postOwner, postOwnerTitle, postOwnerMessage, adminAvatar, dataPayload);
        }

        List<String> followerIds = followRepository.findFollowerIdsByFollowedId(postOwner.getId());

        for (String followerId : followerIds) {
            User follower = profileService.getUserWithProfile(userRepository.findById(followerId).orElseThrow(() -> new RuntimeException("Follower not found")));
            String title = postOwner.getUsername() + " created a new post";
            String message = "Check out " + postOwner.getUsername() + "'s new post: " + savedPost.getTextContent();
            Profile profile = profileService.getProfileByUser(postOwner);
            String avatar = profile.getProfileAvatar().getUrl();

            Notification notification = new Notification();
            notification.setSender(postOwner);
            notification.setReceiver(follower);
            notification.setType(NotificationType.POST);
            notification.setMessage(message);
            notification.setActionUrl("/posts/" + savedPost.getId());
            notificationRepository.save(notification);

            firebaseService.pushNotificationToUser(notification, follower);

            if (follower.getFcmToken() != null && !follower.getId().equals(currentUser.getId())) {
                Map<String, String> dataPayload = Map.of(
                        "type", NotificationType.POST.name(),
                        "postId", savedPost.getId().toString(),
                        "actionUrl", notification.getActionUrl()
                );
                notificationService.sendNotification(follower, title, message, avatar, dataPayload);
            }
        }
        return ResponseEntity.ok("Approved post successfully");
    }

    @Transactional
    public ResponseEntity<?> rejectPost(Long postId, PostStatus postStatus, String rejectionReason) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        Post post = postRepository.findByIdWithoutGroupId(postId).orElseThrow(() -> new RuntimeException("Post not found"));
        if (postStatus.equals(PostStatus.PENDING) || postStatus.equals(PostStatus.APPROVED)) {
            return ResponseEntity.badRequest().body("Invalid post status");
        } else if (post.getStatus().equals(PostStatus.REJECTED)) {
            return ResponseEntity.badRequest().body("Post is already rejected");
        }
        post.setStatus(postStatus);
        post.setReviewedBy(currentUser);
        post.setRejectionReason(rejectionReason);
        Post savedPost = postRepository.save(post);
        User postOwner = profileService.getUserWithProfile(savedPost.getUser());

        String postOwnerTitle = "Your post has been rejected";
        String postOwnerMessage = "Reason: " + rejectionReason + ". Please review and try again.";
        String postOwnerAvatar = "https://firebasestorage.googleapis.com/v0/b/uit-social-network-f592d.appspot.com/o/admin-avatar.jpg?alt=media&token=91551f76-4094-42de-b3c4-2ceb0622e812";
        Notification postOwnernotification = new Notification();
        postOwnernotification.setSender(currentUser);
        postOwnernotification.setReceiver(postOwner);
        postOwnernotification.setType(NotificationType.POST_REJECTION);
        postOwnernotification.setMessage(postOwnerMessage);
        postOwnernotification.setActionUrl("rejected_posts/" + savedPost.getId());
        notificationRepository.save(postOwnernotification);

        firebaseService.pushNotificationToUser(postOwnernotification, postOwner);

        if (postOwner.getFcmToken() != null) {
            Map<String, String> dataPayload = Map.of(
                    "type", NotificationType.POST_REJECTION.name(),
                    "postId", savedPost.getId().toString(),
                    "reason", rejectionReason,
                    "actionUrl", postOwnernotification.getActionUrl()
            );
            notificationService.sendNotification(postOwner, postOwnerTitle, postOwnerMessage, postOwnerAvatar, dataPayload);
        }
        return ResponseEntity.ok("Rejected post successfully");
    }

    public ResponseEntity<?> updateRejectedPost(Long postId, UpdatePostRequest updatePostRequest, List<MultipartFile> mediaFiles) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        Post post = postRepository.findById(postId).orElseThrow(() -> new RuntimeException("Post not found"));
        if (!post.getStatus().equals(PostStatus.REJECTED)) {
            return ResponseEntity.badRequest().body("Post is not rejected");
        }
        if (!post.getUser().getId().equals(currentUser.getId())) {
            return ResponseEntity.badRequest().body("You are not authorized to update this post");
        }
        updatePost(post, updatePostRequest, mediaFiles, currentUser);
        post.setRejectionReason(null);
        post.setStatus(PostStatus.PENDING);
        post.setReviewedBy(null);
        postRepository.save(post);
        return ResponseEntity.ok("Post updated successfully");
    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void deleteOldRejectedPosts() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(30);
        List<Post> postsToDelete = postRepository.findAllByStatusAndUpdatedAtBefore(PostStatus.REJECTED, threshold);
        postRepository.deleteAll(postsToDelete);
    }

    public ResponseEntity<?> getPostsByTopic(Long topicId, int page, int size) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        List<Post> posts = postRepository.findByTopicId(topicId, pageable);
        List<Long> savedPostIds = savedPostRepository.findPostIdsByUserId(currentUser.getId());
        List<PostReaction> reactions = postReactionRepository.findByUserIdAndPostIdIn(currentUser.getId(), posts.stream().map(Post::getId).collect(Collectors.toList()));
        Map<Long, ReactionTypeName> reactionTypeMap = new HashMap<>();
        for (PostReaction reaction : reactions) {
            reactionTypeMap.putIfAbsent(reaction.getPost().getId(), reaction.getReactionType().getName());
        }
        return ResponseEntity.ok(new PostResponse().mapPostsToDTOs(posts, reactionTypeMap, savedPostIds, profileResponseBuilder));
    }
}
