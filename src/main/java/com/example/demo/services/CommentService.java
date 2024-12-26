package com.example.demo.services;

import com.example.demo.dtos.requests.CreateCommentRequest;
import com.example.demo.dtos.responses.CommentResponse;
import com.example.demo.enums.FeedItemType;
import com.example.demo.enums.NotificationType;
import com.example.demo.models.*;
import com.example.demo.repositories.*;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.*;

import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final MediaFileService mediaFileService;
    private final NotificationService notificationService;
    private final NotificationRepository notificationRepository;
    private final FirebaseService firebaseService;
    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;

    @Transactional
    public ResponseEntity<?> createComment(CreateCommentRequest commentRequest, List<MultipartFile> mediaFiles) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        Post post = postRepository.findById(commentRequest.getPostId()).orElseThrow(() -> new RuntimeException("Post not found"));

        User commenter = userRepository.findUserWithProfileById(currentUser.getId())
                .orElseThrow(() -> new RuntimeException("Post owner not found"));
        Comment comment = new Comment();
        comment.setTextContent(commentRequest.getContent());
        comment.setUser(commenter);
        comment.setPost(post);

        Comment savedComment = commentRepository.save(comment);

        if (commentRequest.getParentId() != null) {
            Comment parentComment = commentRepository.findById(commentRequest.getParentId()).orElseThrow(() -> new RuntimeException("Parent comment not found"));
            if (!parentComment.getPost().getId().equals(post.getId())) {
                throw new RuntimeException("Parent comment does not belong to the specified post");
            }
            savedComment.setParentComment(parentComment);
        }

        if (mediaFiles != null && !mediaFiles.isEmpty()) {
            savedComment.setMediaFiles(mediaFileService.uploadMediaFile(savedComment.getId(), FeedItemType.COMMENT, mediaFiles));
        }

        firebaseService.pushCommentToPostOwner(new CommentResponse().toDTO(savedComment));

        User postOwner = post.getUser();
        notifyUserOfComment(postOwner, savedComment, currentUser);
        return ResponseEntity.ok("Comment created successfully");
    }

    public void notifyUserOfComment(User postOwner, Comment comment, User Commenter) {
        // Notify the post owner if the commenter is not the post owner
        if (!postOwner.getId().equals(comment.getUser().getId())) {
            String title = "New Comment";
            String message = "New comment on your post: \"" + comment.getTextContent() + "\"";
            Profile profile = profileRepository.findById(Commenter.getProfile().getId())
                    .orElseThrow(() -> new RuntimeException("Profile not found"));
            String avatar = profile.getProfileAvatar().getUrl();
            String actionUrl = "/posts/" + comment.getPost().getId();

            Notification notification = new Notification();
            notification.setSender(Commenter);
            notification.setReceiver(postOwner);
            notification.setType(NotificationType.COMMENT);
            notification.setMessage(message);
            notification.setActionUrl(actionUrl);
            notificationRepository.save(notification);

            firebaseService.pushNotificationToUser(notification, postOwner);

            if (postOwner.getFcmToken() != null) {
                Map<String, String> dataPayload = Map.of(
                        "type", NotificationType.COMMENT.name(),
                        "postId", comment.getPost().getId().toString(),
                        "commentId", comment.getId().toString(),
                        "actionUrl", actionUrl
                );

                CompletableFuture.runAsync(() -> notificationService.sendNotification(postOwner.getFcmToken(), title, message, avatar, dataPayload));
            }
        }

        // If this is a reply, notify the owner of the parent comment
        if (comment.getParentComment() != null) {
            User parentCommentOwner = comment.getParentComment().getUser();

            // Avoid sending the notification twice if the parent comment owner is also the post owner
            if (!parentCommentOwner.getId().equals(postOwner.getId()) &&
                    !parentCommentOwner.getId().equals(comment.getUser().getId())) {

                String title = "New Reply";
                String replyMessage = "New reply to your comment: \"" + comment.getTextContent() + "\"";
                Profile profile = profileRepository.findById(Commenter.getProfile().getId())
                        .orElseThrow(() -> new RuntimeException("Profile not found"));
                String avatar = profile.getProfileAvatar().getUrl();
                String actionUrl = "/posts/" + comment.getPost().getId();

                Notification notification = new Notification();
                notification.setSender(Commenter);
                notification.setReceiver(parentCommentOwner);
                notification.setType(NotificationType.REPLY_COMMENT);
                notification.setMessage(replyMessage);
                notification.setActionUrl(actionUrl);
                notificationRepository.save(notification);

                firebaseService.pushNotificationToUser(notification, parentCommentOwner);

                if (parentCommentOwner.getFcmToken() != null) {
                    Map<String, String> dataPayload = Map.of(
                            "type", NotificationType.REPLY_COMMENT.name(),
                            "postId", comment.getPost().getId().toString(),
                            "commentId", comment.getId().toString(),
                            "actionUrl", actionUrl
                    );

                    CompletableFuture.runAsync(() -> notificationService.sendNotification(parentCommentOwner.getFcmToken(), title, replyMessage, avatar, dataPayload));
                }
            }
        }
    }

    public ResponseEntity<PagedModel<CommentResponse>> getCommentsWithReplies(Long postId, int page, int size, PagedResourcesAssembler assembler) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Comment> topLevelComments = commentRepository.findByPostIdAndParentCommentIsNull(postId, pageable);
        List<Long> topLevelCommentIds = topLevelComments.getContent().stream()
                .map(Comment::getId)
                .collect(Collectors.toList());

        // Fetch replies for top-level comments up to the specified depth
        List<Comment> replies = commentRepository.findByParentCommentIdIn(topLevelCommentIds);

        // Group replies by parent comment ID
        Map<Long, List<Comment>> repliesMap = replies.stream()
                .collect(Collectors.groupingBy(comment -> comment.getParentComment().getId()));

        // Convert top-level comments to DTOs with controlled depth
        List<CommentResponse> commentResponses = topLevelComments.getContent().stream()
                .map(comment -> toDTOWithDepth(comment, repliesMap, 1, 2))
                .collect(Collectors.toList());

        Page<CommentResponse> commentResponsePage = new PageImpl<>(commentResponses, pageable, topLevelComments.getTotalElements());
        PagedModel<CommentResponse> pagedModel = assembler.toModel(commentResponsePage);
        return ResponseEntity.ok(pagedModel);
    }

    public CommentResponse toDTOWithDepth(Comment comment, Map<Long, List<Comment>> repliesMap, int currentDepth, int maxDepth) {
        CommentResponse commentResponse = new CommentResponse();
        commentResponse.toDTO(comment);

        // Load replies only if the current depth is less than the max depth
        if (currentDepth < maxDepth) {
            List<Comment> replies = repliesMap.get(comment.getId());
            if (replies != null) {
                commentResponse.setReplies(replies.stream()
                        .map(reply -> toDTOWithDepth(reply, repliesMap, currentDepth + 1, maxDepth))
                        .collect(Collectors.toList()));
            }
        }
        return commentResponse;
    }

    public ResponseEntity<PagedModel<CommentResponse>> getReplies(Long parentCommentId, int page, int size, PagedResourcesAssembler assembler) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").ascending());
        Page<Comment> repliesPage = commentRepository.findByParentCommentId(parentCommentId, pageable);

        List<CommentResponse> replies = repliesPage.getContent().stream()
                .map(comment -> new CommentResponse().toDTO(comment))
                .collect(Collectors.toList());

        Page<CommentResponse> commentResponsePage = new PageImpl<>(replies, pageable, repliesPage.getTotalElements());
        PagedModel<CommentResponse> pagedModel = assembler.toModel(commentResponsePage);
        return ResponseEntity.ok(pagedModel);
    }
}
