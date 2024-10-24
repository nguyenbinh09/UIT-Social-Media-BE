package com.example.demo.dtos.responses;

import com.example.demo.models.Comment;
import com.example.demo.models.MediaFile;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentResponse {
    private Long id;
    private UserResponse user;
    private PostResponse post;
    private String parentId;
    private String textContent;
    private List<String> mediaFiles;
    private LocalDateTime createdAt;

    public CommentResponse toDTO(Comment comment) {
        CommentResponse commentResponse = new CommentResponse();
        commentResponse.setId(comment.getId());
        commentResponse.setUser(new UserResponse().toDTO(comment.getUser()));
        commentResponse.setPost(new PostResponse().toDTO(comment.getPost()));
        commentResponse.setParentId(comment.getParentId());
        commentResponse.setTextContent(comment.getTextContent());
        commentResponse.setMediaFiles(comment.getMediaFiles().stream().map(MediaFile::getUrl).toList());
        commentResponse.setCreatedAt(comment.getCreatedAt());
        return commentResponse;
    }

    public List<CommentResponse> mapCommentsToDTOs(List<Comment> comments) {
        return comments.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
}
