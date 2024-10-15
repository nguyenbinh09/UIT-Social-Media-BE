package com.example.demo.repositories;

import com.example.demo.enums.ReactionTypeName;
import com.example.demo.enums.RoleName;
import com.example.demo.models.ReactionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReactionTypeRepository extends JpaRepository<ReactionType, Long> {

    Optional<ReactionType> findByName(ReactionTypeName typeNameEnum);
}
