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
    private List<String> mediaFiles;
    private ReactionTypeName reactionType;
    private LocalDateTime createdAt;

    public PostResponse toDTO(Post post) {
        PostResponse postResponse = new PostResponse();
        postResponse.setId(post.getId());
        postResponse.setTextContent(post.getTextContent());
        postResponse.setTitle(post.getTitle());
        postResponse.setPrivacy(new PrivacyResponse().toDTO(post.getPrivacy()));
        postResponse.setUser(new UserResponse().toDTO(post.getUser()));
        postResponse.setMediaFiles(post.getMediaFiles().stream().map(MediaFile::getUrl).toList());
        postResponse.setCreatedAt(post.getCreatedAt());
        return postResponse;
    }

    public PostResponse toDTOWithReaction(Post post, ReactionTypeName reactionType) {
        PostResponse postResponse = toDTO(post);
        postResponse.setReactionType(reactionType);
        return postResponse;
    }

    public List<PostResponse> mapPostsToDTOs(List<Post> posts, Map<Long, ReactionTypeName> reactionTypeMap) {
        return posts.stream()
                .map(post -> toDTOWithReaction(post, reactionTypeMap.get(post.getId())))
                .collect(Collectors.toList());
    }
}

