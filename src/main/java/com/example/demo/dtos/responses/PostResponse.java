package com.example.demo.dtos.responses;

import com.example.demo.enums.PostStatus;
import com.example.demo.enums.ReactionTypeName;
import com.example.demo.models.MediaFile;
import com.example.demo.models.Post;
import com.example.demo.models.Topic;
import com.example.demo.services.ProfileResponseBuilder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostResponse {
    private Long id;
    private String textContent;
    private String title;
    private String link;
    private PrivacyResponse privacy;
    private UserResponse user;
    private GroupResponse group;
    private List<MediaFileResponse> mediaFiles;
    private ReactionTypeName reactionType;
    private LocalDateTime createdAt;
    private PostResponse sharedPost;
    private Boolean isSaved;
    private PostStatus status;
    private int reactionCount;
    private int commentCount;
    private List<String> topics;

    public PostResponse toDTO(Post post, ProfileResponseBuilder profileResponseBuilder) {
        this.setId(post.getId());
        this.setTextContent(post.getTextContent());
        this.setTitle(post.getTitle());
        this.setPrivacy(new PrivacyResponse().toDTO(post.getPrivacy()));
        this.setUser(new UserResponse().toDTO(post.getUser(), profileResponseBuilder));
        if (post.getGroup() != null) {
            this.setGroup(new GroupResponse().toDTO(post.getGroup()));
        }
        this.setMediaFiles(new MediaFileResponse().mapsToDto(post.getMediaFiles()));
        this.setReactionCount(post.getReactions().size());
        this.setCommentCount(post.getComments().size());
        this.setCreatedAt(post.getCreatedAt());
        this.setStatus(post.getStatus());
        this.setLink(post.getLink());
        this.setTopics(post.getTopics().stream().map(Topic::getName).collect(Collectors.toList()));
        if (post.getIsShared())
            this.setSharedPost(new PostResponse().toDTO(post.getSharedPost(), profileResponseBuilder));
        return this;
    }

    public PostResponse toDTOWithReaction(Post post, ReactionTypeName reactionType, ProfileResponseBuilder profileResponseBuilder) {
        this.toDTO(post, profileResponseBuilder);
        this.setReactionType(reactionType);
        return this;
    }

    public List<PostResponse> mapPostsToDTOs(List<Post> posts, Map<Long, ReactionTypeName> reactionTypeMap, List<Long> savedPostIds, ProfileResponseBuilder profileResponseBuilder) {
        return posts.stream().map(post -> {
            PostResponse postResponse = new PostResponse().toDTOWithReaction(post, reactionTypeMap.get(post.getId()), profileResponseBuilder);
            postResponse.setIsSaved(savedPostIds.contains(post.getId()));
            return postResponse;
        }).collect(Collectors.toList());
    }
}

