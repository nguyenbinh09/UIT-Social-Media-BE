package com.example.demo.services;

import com.example.demo.enums.InteractionType;
import com.example.demo.models.Post;
import com.example.demo.models.User;
import com.example.demo.models.UserInteraction;
import com.example.demo.repositories.UserInteractionRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@AllArgsConstructor
public class UserInteractionService {
    private final UserInteractionRepository userInteractionRepository;

    @Transactional
    public void createUserInteraction(User user, Post post, InteractionType interactionType) {
        if (userInteractionRepository.findByUserIdAndPostIdAndInteractionType(user.getId(), post.getId(), interactionType).isEmpty()) {
            UserInteraction interaction = new UserInteraction();
            interaction.setUser(user);
            interaction.setPost(post);
            interaction.setInteractionType(interactionType);
            interaction.setTimestamp(LocalDateTime.now());
            switch (interactionType) {
                case LIKE:
                    interaction.setRating(5.0);
                    break;
                case COMMENT:
                    interaction.setRating(4.0);
                    break;
                case SHARE:
                    interaction.setRating(3.0);
                    break;
                case VIEW:
                    interaction.setRating(2.0);
                    break;
            }
            userInteractionRepository.save(interaction);
        }
    }

    public void updateUserInteraction(User user, Post post, InteractionType interactionType) {
        Optional<UserInteraction> interactionOpt = userInteractionRepository.findByUserIdAndPostId(user.getId(), post.getId());
        if (interactionOpt.isPresent()) {
            UserInteraction interaction = interactionOpt.get();
            interaction.setTimestamp(LocalDateTime.now());
            interaction.setInteractionType(interactionType);
            userInteractionRepository.save(interaction);
        } else {
            createUserInteraction(user, post, interactionType);
        }
    }

    @Transactional
    public void deleteUserInteraction(User user, Post post, InteractionType interactionType) {
        Optional<UserInteraction> interactionOpt = userInteractionRepository.findByUserIdAndPostIdAndInteractionType(
                user.getId(), post.getId(), interactionType);
        interactionOpt.ifPresent(userInteractionRepository::delete);
    }
}
