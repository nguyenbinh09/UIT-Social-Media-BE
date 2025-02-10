package com.example.demo.repositories;

import com.example.demo.models.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TopicRepository extends JpaRepository<Topic, Long> {
    Optional<Topic> findByName(String name);

    @Query("SELECT t.name, COUNT(p.id) " +
            "FROM Topic t " +
            "JOIN t.posts p " +
            "GROUP BY t.name " +
            "ORDER BY COUNT(p.id) DESC")
    List<Object[]> countPostsByTopic();
}
