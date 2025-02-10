package com.example.demo.services;

import com.example.demo.enums.InteractionType;
import com.example.demo.repositories.*;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class StatisticsService {
    private PostRepository postRepository;
    private CommentRepository commentRepository;
    private UserInteractionRepository userInteractionRepository;
    private TopicRepository topicRepository;

    public ResponseEntity<?> getStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        long totalPosts = postRepository.countPostsBetweenDates(startDate, endDate);

        long totalComments = commentRepository.countCommentsBetweenDates(startDate, endDate);

        long totalLikes = userInteractionRepository.countInteractionsBetweenDates(
                InteractionType.LIKE, startDate, endDate);

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalPosts", totalPosts);
        statistics.put("totalComments", totalComments);
        statistics.put("totalLikes", totalLikes);

        return ResponseEntity.ok(statistics);
    }


    public Map<String, Long> getTopicPostStatistics() {
        List<Object[]> rawData = topicRepository.countPostsByTopic();

        return rawData.stream()
                .collect(Collectors.toMap(
                        obj -> (String) obj[0],
                        obj -> (Long) obj[1]
                ));
    }
}
