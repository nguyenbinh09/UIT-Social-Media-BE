package com.example.demo.dtos.responses;

import com.example.demo.models.Comment;
import com.example.demo.models.MediaFile;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private List<String> mediaFiles;
    private LocalDateTime createdAt;

    public CommentResponse toDTO(Comment comment) {
        this.setId(comment.getId());
        this.setUser(new UserResponse().toDTO(comment.getUser()));
        this.setPostId(comment.getPost().getId());

        if (comment.getParentComment() != null) {
            this.setParentId(comment.getParentComment().getId());
        } else {
            this.setParentId(null);
        }

        this.setTextContent(comment.getTextContent());
        this.setMediaFiles(comment.getMediaFiles().stream().map(MediaFile::getUrl).toList());
        this.setCreatedAt(comment.getCreatedAt());
        return this;
    }

    public List<CommentResponse> mapCommentsToDTOs(List<Comment> comments) {
        return comments.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }


}
