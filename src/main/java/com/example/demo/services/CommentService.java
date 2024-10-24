package com.example.demo.services;

import com.example.demo.dtos.requests.CreateCommentRequest;
import com.example.demo.enums.FeedItemType;
import com.example.demo.models.Comment;
import com.example.demo.models.Post;
import com.example.demo.models.Privacy;
import com.example.demo.models.User;
import com.example.demo.repositories.CommentRepository;
import com.example.demo.repositories.PostReposiroty;
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
    private final PostReposiroty postReposiroty;
    private final MediaFileService mediaFileService;

    public ResponseEntity<?> createComment(CreateCommentRequest commentRequest, List<MultipartFile> mediaFiles) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        Post post = postReposiroty.findById(commentRequest.getPostId()).orElseThrow(() -> new RuntimeException("Post not found"));

        Comment comment = new Comment();
        comment.setTextContent(commentRequest.getContent());
        comment.setUser(currentUser);
        comment.setPost(post);
        if (commentRequest.getParentId() != null) {
            Comment parentComment = commentRepository.findById(commentRequest.getParentId()).orElseThrow(() -> new RuntimeException("Parent comment not found"));
            comment.setParentComment(parentComment);
        }
        if (mediaFiles != null && !mediaFiles.isEmpty()) {
            comment.setMediaFiles(mediaFileService.uploadMediaFile(comment.getId(), FeedItemType.COMMENT, mediaFiles));
        }
        commentRepository.save(comment);
        return ResponseEntity.ok("Comment created successfully");
    }
}
