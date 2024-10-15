package com.example.demo.services;

import com.example.demo.dtos.requests.ReactPostRequest;
import com.example.demo.models.Post;
import com.example.demo.models.PostReaction;
import com.example.demo.models.ReactionType;
import com.example.demo.models.User;
import com.example.demo.repositories.*;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class ReactionService {
    private final ReactionRepository reactionRepository;
    private final PostReactionRepository postReactionRepository;
    private final PostReposiroty postRepository;
    private final UserRepository userRepository;
    private final ReactionTypeRepository reactionTypeRepository;


    @Transactional
    public ResponseEntity<?> reactPost(ReactPostRequest reactPostRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        Post post = postRepository.findById(reactPostRequest.getPostId())
                .orElseThrow(() -> new RuntimeException("Post not found"));
        ReactionType reactionType = reactionTypeRepository.findById(reactPostRequest.getReactionTypeId())
                .orElseThrow(() -> new RuntimeException("Reaction not found"));

        Optional<PostReaction> postReactionOpt = postReactionRepository.findByPostIdAndUserId(post.getId(), currentUser.getId());

        if (postReactionOpt.isPresent()) {
            PostReaction existingPostReaction = postReactionOpt.get();
            existingPostReaction.setReactionType(reactionType);
            postReactionRepository.save(existingPostReaction);
            return ResponseEntity.ok("Reaction updated successfully");
        } else {
            PostReaction newPostReaction = new PostReaction();
            newPostReaction.setPost(post);
            newPostReaction.setUser(currentUser);
            newPostReaction.setReactionType(reactionType);
            postReactionRepository.save(newPostReaction);
            return ResponseEntity.ok("Reaction created successfully");
        }
    }
    public void deleteReaction(Long postReactionId) {
        postReactionRepository.deleteById(postReactionId);
    }
}
