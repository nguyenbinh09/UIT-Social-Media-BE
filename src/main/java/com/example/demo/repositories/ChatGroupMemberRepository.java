package com.example.demo.repositories;

import com.example.demo.models.ChatGroupMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ChatGroupMemberRepository extends JpaRepository<ChatGroupMember, Long> {
    Optional<ChatGroupMember> findByChatGroupIdAndUserId(Long chatGroupId, String id);

    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN TRUE ELSE FALSE END FROM ChatGroupMember c WHERE c.chatGroup.id = ?1 AND c.user.id = ?2")
    Boolean existsByChatGroupIdAndUserId(Long chatGroupId, String id);
}
