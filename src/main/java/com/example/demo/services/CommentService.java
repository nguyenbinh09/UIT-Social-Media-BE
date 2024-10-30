package com.example.demo.services;

import com.example.demo.dtos.requests.CreateCommentRequest;
import com.example.demo.enums.FeedItemType;
import com.example.demo.models.Comment;
import com.example.demo.models.Post;
import com.example.demo.models.User;
import com.example.demo.repositories.CommentRepository;
import com.example.demo.repositories.PostRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@AllArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final MediaFileService mediaFileService;
    private final NotificationService notificationService;
    private final FirebaseService firebaseService;

    @Transactional
    public ResponseEntity<?> createComment(CreateCommentRequest commentRequest, List<MultipartFile> mediaFiles) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        Post post = postRepository.findById(commentRequest.getPostId()).orElseThrow(() -> new RuntimeException("Post not found"));

        Comment comment = new Comment();
        comment.setTextContent(commentRequest.getContent());
        comment.setUser(currentUser);
        comment.setPost(post);

        Comment savedComment = commentRepository.save(comment);

        if (commentRequest.getParentId() != null) {
            Comment parentComment = commentRepository.findById(commentRequest.getParentId()).orElseThrow(() -> new RuntimeException("Parent comment not found"));
            savedComment.setParentComment(parentComment);
        }
        System.out.println("mediaFiles: " + mediaFiles);
        if (mediaFiles != null && !mediaFiles.isEmpty()) {
            comment.setMediaFiles(mediaFileService.uploadMediaFile(savedComment.getId(), FeedItemType.COMMENT, mediaFiles));
        }

        firebaseService.pushCommentToPostOwner(comment);

        User postOwner = post.getUser();
        notifyUserOfComment(postOwner, comment);
        return ResponseEntity.ok("Comment created successfully");
    }

    public void notifyUserOfComment(User postOwner, Comment comment) {
        // Notify the post owner if the commenter is not the post owner
        if (!postOwner.getId().equals(comment.getUser().getId())) {
            String message = "New comment on your post: " + comment.getTextContent();
            notificationService.sendNotification(postOwner.getFcmToken(), "New Comment", message);
        }

        // If this is a reply, notify the owner of the parent comment
        if (comment.getParentComment() != null) {
            User parentCommentOwner = comment.getParentComment().getUser();

            // Avoid sending the notification twice if the parent comment owner is also the post owner
            if (!parentCommentOwner.getId().equals(postOwner.getId()) &&
                    !parentCommentOwner.getId().equals(comment.getUser().getId())) {

                String replyMessage = "New reply to your comment: " + comment.getTextContent();
                notificationService.sendNotification(parentCommentOwner.getFcmToken(), "New Reply", replyMessage);
            }
        }
    }
}
