package com.example.demo.services;

import com.example.demo.dtos.requests.CreateReactionTypeRequest;
import com.example.demo.enums.ReactionTypeName;
import com.example.demo.enums.RoleName;
import com.example.demo.models.ReactionType;
import com.example.demo.repositories.ReactionTypeRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class ReactionTypeService {
    private final ReactionTypeRepository reactionTypeRepository;
    @Transactional
    public ResponseEntity<?> createReactionType(CreateReactionTypeRequest createReactionTypeRequest){
        String typeNameString = createReactionTypeRequest.getName();
        ReactionTypeName typeNameEnum;
        try {
            typeNameEnum = ReactionTypeName.valueOf(typeNameString);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid role: " + typeNameString);
        }

        Optional<ReactionType> reactionType = reactionTypeRepository.findByName(typeNameEnum);
        if (reactionType.isPresent()) {
            throw new RuntimeException("Reaction type already exists: " + typeNameEnum);
        }

        ReactionType newReactionType = new ReactionType();
        newReactionType.setName(typeNameEnum);
        return ResponseEntity.ok(reactionTypeRepository.save(newReactionType));
    }
}
