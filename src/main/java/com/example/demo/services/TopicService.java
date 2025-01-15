package com.example.demo.services;

import com.example.demo.dtos.requests.CreateTopicRequest;
import com.example.demo.dtos.responses.TopicResponse;
import com.example.demo.models.Topic;
import com.example.demo.repositories.TopicRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class TopicService {
    private final TopicRepository topicRepository;

    public ResponseEntity<?> createTopic(CreateTopicRequest createTopicRequest) {
        if (topicRepository.findByName(createTopicRequest.getName()).isPresent()) {
            throw new RuntimeException("Topic already exists");
        }
        Topic topic = new Topic();
        topic.setName(createTopicRequest.getName());
        topic.setDescription(createTopicRequest.getDescription());
        TopicResponse topicResponse = new TopicResponse().toDTO(topicRepository.save(topic));
        return ResponseEntity.ok(topicResponse);
    }

    public ResponseEntity<?> getTopics() {
        List<Topic> topics = topicRepository.findAll();
        List<TopicResponse> topicResponses = topics.stream().map(topic -> new TopicResponse().toDTO(topic)).collect(Collectors.toList());
        return ResponseEntity.ok(topicResponses);
    }

    public ResponseEntity<?> getTopic(Long id) {
        Topic topic = topicRepository.findById(id).orElseThrow(() -> new RuntimeException("Topic not found"));
        TopicResponse topicResponse = new TopicResponse().toDTO(topic);
        return ResponseEntity.ok(topicResponse);
    }

    public ResponseEntity<?> updateTopic(Long id, CreateTopicRequest createTopicRequest) {
        Topic topic = topicRepository.findById(id).orElseThrow(() -> new RuntimeException("Topic not found"));
        if (topicRepository.findByName(createTopicRequest.getName()).isPresent()) {
            throw new RuntimeException("Topic already exists");
        }
        if (createTopicRequest.getName() != null) {
            topic.setName(createTopicRequest.getName());
        }
        if (createTopicRequest.getDescription() != null) {
            topic.setDescription(createTopicRequest.getDescription());
        }
        TopicResponse topicResponse = new TopicResponse().toDTO(topicRepository.save(topic));
        return ResponseEntity.ok(topicResponse);
    }

    public ResponseEntity<?> deleteTopic(Long id) {
        Topic topic = topicRepository.findById(id).orElseThrow(() -> new RuntimeException("Topic not found"));
        topicRepository.delete(topic);
        return ResponseEntity.ok("Topic deleted");
    }
}
