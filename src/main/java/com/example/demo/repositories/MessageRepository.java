package com.example.demo.repositories;

import com.example.demo.models.ChatGroup;
import com.example.demo.models.ChatbotConversation;
import com.example.demo.models.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    boolean existsByConversationId(Long id);

    List<Message> findByChatGroup(ChatGroup chatGroup, Pageable pageable);

    List<Message> findByConversationId(Long conversationId, Pageable pageable);

    List<Message> findByChatbotConversation(ChatbotConversation conversation, Pageable pageable);
}
