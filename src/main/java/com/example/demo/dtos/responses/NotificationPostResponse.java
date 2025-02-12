package com.example.demo.dtos.responses;

import com.example.demo.enums.PostStatus;
import com.example.demo.enums.ReactionTypeName;
import com.example.demo.models.Post;
import com.example.demo.models.Topic;
import com.example.demo.services.ProfileResponseBuilder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NotificationPostResponse {
    private Long id;
    private String textContent;
    private String title;
    private String link;
    private PrivacyResponse privacy;
    private UserResponse user;
    private List<MediaFileResponse> mediaFiles;
    private ReactionTypeName reactionType;
    private LocalDateTime createdAt;
    private PostStatus status;
    private int reactionCount;
    private int commentCount;
    private List<String> topics;

    public NotificationPostResponse toDTO(Post post, ProfileResponseBuilder profileResponseBuilder) {
        this.setId(post.getId());
        this.setTextContent(post.getTextContent());
        this.setTitle(post.getTitle());
        this.setPrivacy(new PrivacyResponse().toDTO(post.getPrivacy()));
        this.setUser(new UserResponse().toDTO(post.getUser(), profileResponseBuilder));
        this.setMediaFiles(new MediaFileResponse().mapsToDto(post.getMediaFiles()));
        this.setReactionCount(post.getReactions().size());
        this.setCommentCount(post.getComments().size());
        this.setCreatedAt(post.getCreatedAt());
        this.setStatus(post.getStatus());
        this.setLink(post.getLink());
        this.setTopics(post.getTopics().stream().map(Topic::getName).collect(Collectors.toList()));
        return this;
    }
}
