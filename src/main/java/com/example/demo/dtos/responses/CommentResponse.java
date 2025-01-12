package com.example.demo.dtos.responses;

import com.example.demo.models.Comment;
import com.example.demo.models.MediaFile;
import com.example.demo.services.ProfileResponseBuilder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.Hibernate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentResponse {
    private Long id;
    private UserResponse user;
    private Long postId;
    private Long parentId;
    private String textContent;
    private List<CommentResponse> replies;
    private List<MediaFileResponse> mediaFiles;
    private LocalDateTime createdAt;
    private int replyCount;

    public CommentResponse toDTO(Comment comment, ProfileResponseBuilder profileResponseBuilder) {
        this.setId(comment.getId());
        this.setUser(new UserResponse().toDTO(comment.getUser(), profileResponseBuilder));
        this.setPostId(comment.getPost().getId());

        if (comment.getParentComment() != null) {
            this.setParentId(comment.getParentComment().getId());
        } else {
            this.setParentId(null);
        }
        this.setTextContent(comment.getTextContent());
        this.setMediaFiles(new MediaFileResponse().mapsToDto(comment.getMediaFiles()));
        this.setCreatedAt(comment.getCreatedAt());
        this.setReplyCount(comment.getReplies() != null ? comment.getReplies().size() : 0);
        return this;
    }

    public List<CommentResponse> mapCommentsToDTOs(List<Comment> comments, ProfileResponseBuilder profileResponseBuilder) {
        return comments.stream()
                .map(comment -> new CommentResponse().toDTO(comment, profileResponseBuilder))
                .collect(Collectors.toList());
    }


}
