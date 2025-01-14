package com.example.demo.repositories;

import com.example.demo.models.PersonalConversation;
import com.example.demo.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PersonalConversationRepository extends JpaRepository<PersonalConversation, Long> {
    Optional<PersonalConversation> findByUser1IdAndUser2Id(String user1Id, String user2Id);

    @Query("SELECT pc FROM PersonalConversation pc WHERE " +
            "((pc.user1.id = :userId1 AND pc.user2.id = :userId2) OR " +
            "(pc.user1.id = :userId2 AND pc.user2.id = :userId1)) " +
            "AND pc.isDeleted = false")
    Optional<PersonalConversation> findByUserIds(String userId1, String userId2);

    @Query("SELECT c FROM PersonalConversation c WHERE (c.user1.id = :userId OR c.user2.id = :userId) " +
            "AND c.isPending = false " +
            "ORDER BY (SELECT MAX(msg.createdAt) FROM Message msg WHERE msg.conversation = c) DESC")
    List<PersonalConversation> findConversationsWithLatestMessages(@Param("userId") String userId);

    @Query("SELECT c FROM PersonalConversation c WHERE (c.user1.id = :userId OR c.user2.id = :userId) " +
            "AND c.isPending = true " +
            "ORDER BY (SELECT MAX(msg.createdAt) FROM Message msg WHERE msg.conversation = c) DESC")
    List<PersonalConversation> findPendingConversationsWithLatestMessages(@Param("userId") String userId);

}
