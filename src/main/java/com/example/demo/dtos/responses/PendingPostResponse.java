package com.example.demo.dtos.responses;

import com.example.demo.enums.PostStatus;
import com.example.demo.enums.PrivacyName;
import com.example.demo.models.Post;
import com.example.demo.services.ProfileResponseBuilder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PendingPostResponse {
    private Long id;
    private String textContent;
    private String title;
    private UserResponse user;
    private String createdAt;
    private PostStatus status;
    private PrivacyName privacy;
    private List<MediaFileResponse> mediaFiles;
    private String link;

    public PendingPostResponse toDTO(Post post, ProfileResponseBuilder profileResponseBuilder) {
        this.setId(post.getId());
        this.setTextContent(post.getTextContent());
        this.setTitle(post.getTitle());
        this.setPrivacy(post.getPrivacy().getName());
        this.setUser(new UserResponse().toDTO(post.getUser(), profileResponseBuilder));
        this.setMediaFiles(new MediaFileResponse().mapsToDto(post.getMediaFiles()));
        this.setCreatedAt(post.getCreatedAt().toString());
        this.setStatus(post.getStatus());
        this.setLink(post.getLink());
        return this;
    }

    public List<PendingPostResponse> mapPostsToDTOs(List<Post> pendingPosts, ProfileResponseBuilder profileResponseBuilder) {
        return pendingPosts.stream().map(post -> new PendingPostResponse().toDTO(post, profileResponseBuilder)).toList();
    }
}
