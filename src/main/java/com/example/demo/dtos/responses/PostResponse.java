package com.example.demo.dtos.responses;

import com.example.demo.enums.ReactionTypeName;
import com.example.demo.models.MediaFile;
import com.example.demo.models.Post;
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
    private PrivacyResponse privacy;
    private UserResponse user;
    private GroupResponse group;
    private List<MediaFileResponse> mediaFiles;
    private ReactionTypeName reactionType;
    private LocalDateTime createdAt;
    private PostResponse sharedPost;
    private Boolean isSaved;
    private int reactionCount;
    private int commentCount;

    public PostResponse toDTO(Post post) {
        this.setId(post.getId());
        this.setTextContent(post.getTextContent());
        this.setTitle(post.getTitle());
        this.setPrivacy(new PrivacyResponse().toDTO(post.getPrivacy()));
        this.setUser(new UserResponse().toDTO(post.getUser()));
        if (post.getGroup() != null) {
            this.setGroup(new GroupResponse().toDTO(post.getGroup()));
        }
        this.setMediaFiles(new MediaFileResponse().mapsToDto(post.getMediaFiles()));
        this.setReactionCount(post.getReactions().size());
        this.setCommentCount(post.getComments().size());

        this.setCreatedAt(post.getCreatedAt());
        if (post.getIsShared())
            this.setSharedPost(new PostResponse().toDTO(post.getSharedPost()));
        return this;
    }

    public PostResponse toDTOWithReaction(Post post, ReactionTypeName reactionType) {
        PostResponse postResponse = new PostResponse();
        postResponse.toDTO(post);
        postResponse.setReactionType(reactionType);
        return postResponse;
    }

    public List<PostResponse> mapPostsToDTOs(List<Post> posts, Map<Long, ReactionTypeName> reactionTypeMap) {
        return posts.stream()
                .map(post -> toDTOWithReaction(post, reactionTypeMap.get(post.getId())))
                .collect(Collectors.toList());
    }
}

