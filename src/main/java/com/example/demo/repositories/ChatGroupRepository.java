package com.example.demo.repositories;

import com.example.demo.models.ChatGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatGroupRepository extends JpaRepository<ChatGroup, Long> {
    @Query("SELECT g FROM ChatGroup g JOIN g.members m WHERE m.user.id = :userId " +
            "ORDER BY (SELECT MAX(msg.createdAt) FROM Message msg WHERE msg.chatGroup = g) DESC")
    List<ChatGroup> findChatGroupsWithLatestMessages(@Param("userId") String userId);
}
