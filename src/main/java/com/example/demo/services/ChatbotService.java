package com.example.demo.services;

import com.example.demo.dtos.responses.ChatbotMessageResponse;
import com.example.demo.models.ChatbotConversation;
import com.example.demo.models.Message;
import com.example.demo.models.User;
import com.example.demo.repositories.ChatbotConversationRepository;
import com.example.demo.repositories.MessageRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.SocketTimeoutException;
import java.util.List;
import java.util.Map;

@Service
public class ChatbotService {
    private final ChatbotConversationRepository chatbotConversationRepository;
    private final MessageRepository messageRepository;
    private final RestTemplate restTemplate;

    public ChatbotService(ChatbotConversationRepository chatbotConversationRepository, MessageRepository messageRepository, RestTemplate restTemplate) {
        this.chatbotConversationRepository = chatbotConversationRepository;
        this.messageRepository = messageRepository;
        this.restTemplate = restTemplate;
    }

    @Value("${uit-chatbot.url}")
    private String chatbotUrl;

    public ChatbotConversation getOrCreateConversation(User user) {
        return chatbotConversationRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    ChatbotConversation conversation = new ChatbotConversation();
                    conversation.setUser(user);
                    return chatbotConversationRepository.save(conversation);
                });
    }

    @Transactional
    public ResponseEntity<?> sendMessageToChatbot(String content) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        ChatbotConversation conversation = getOrCreateConversation(currentUser);

        Message message = new Message();
        message.setSender(currentUser);
        message.setChatbotConversation(conversation);
        message.setContent(content);
        message.setIsRead(true);

        messageRepository.save(message);

        String chatbotResponse = generateChatbotResponse(content);

        Message responseMessage = new Message();
        responseMessage.setSender(null);
        responseMessage.setChatbotConversation(conversation);
        responseMessage.setContent(chatbotResponse);

        messageRepository.save(responseMessage);

        ChatbotMessageResponse chatbotMessageResponse = new ChatbotMessageResponse().toDTO(responseMessage);
        return ResponseEntity.ok(chatbotMessageResponse);
    }

    private String generateChatbotResponse(String userMessage) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> request = Map.of("msg", userMessage);
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(request, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(chatbotUrl, entity, Map.class);

            Map<String, Object> responseBody = response.getBody();
            if (responseBody != null) {
                Object msgObject = responseBody.get("msg");
                if (msgObject instanceof Map) {
                    Map<String, Object> msgMap = (Map<String, Object>) msgObject;
                    Map<String, Object> mainMap = (Map<String, Object>) msgMap.get("main");
                    if (mainMap != null) {
                        return (String) mainMap.get("content");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "An error occurred while communicating with the chatbot.";
        }

        return "Sorry, I couldn't process your request.";
    }

    public ResponseEntity<?> getChatbotConversation(int page, int size) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        ChatbotConversation conversation = getOrCreateConversation(currentUser);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Order.desc("createdAt")));
        List<Message> messages = messageRepository.findByChatbotConversation(conversation, pageable);
        List<ChatbotMessageResponse> chatbotMessageResponses = new ChatbotMessageResponse().mapsToDto(messages);
        return ResponseEntity.ok(chatbotMessageResponses);
    }
}
