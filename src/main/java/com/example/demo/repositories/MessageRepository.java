package com.example.demo.repositories;

import com.example.demo.models.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.awt.print.Pageable;
import java.util.List;
import java.util.Optional;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    boolean existsByConversationId(Long id);
//    List<Message> findByConversationId(Long conversationId, Pageable pageable);
}
