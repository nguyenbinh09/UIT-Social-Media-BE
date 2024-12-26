package com.example.demo.services;

import com.example.demo.dtos.responses.CommentResponse;
import com.example.demo.dtos.responses.MessageResponse;
import com.example.demo.dtos.responses.PostResponse;
import com.example.demo.models.Comment;
import com.example.demo.models.Notification;
import com.example.demo.models.Post;
import com.example.demo.models.User;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.storage.Acl;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.firebase.FirebaseApp;
import com.google.firebase.cloud.FirestoreClient;
import com.google.firebase.cloud.StorageClient;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MulticastMessage;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class FirebaseService {
    private final DatabaseReference firebaseDatabase;
    private final FirebaseApp firebaseApp;
    private final String bucketName;
    private final ObjectMapper objectMapper;
    private final NotificationService notificationService;
    private final Firestore firestore = FirestoreClient.getFirestore();

    @Autowired
    public FirebaseService(@Value("${firebase.bucket.name}") String bucketName, DatabaseReference firebaseDatabase, FirebaseApp firebaseApp, ObjectMapper objectMapper, NotificationService notificationService) {
        this.firebaseDatabase = firebaseDatabase;
        this.firebaseApp = firebaseApp;
        this.bucketName = bucketName;
        this.objectMapper = objectMapper;
        this.notificationService = notificationService;
    }

    @Transactional
    public void pushPostToReceivers(Post post, List<String> receiverIds) {
        PostResponse postResponse = new PostResponse().toDTO(post);
        Map<String, Object> postResponseMap = objectMapper.convertValue(postResponse, new TypeReference<>() {
        });

        firestore.collection("posts")
                .document(postResponse.getId().toString())
                .set(postResponseMap);

        for (String receiverId : receiverIds) {
            Map<String, Object> postReferenceMap = Map.of(
                    "post_id", post.getId(),
                    "title", postResponse.getTitle(),
                    "timestamp", postResponse.getCreatedAt().toEpochSecond(ZoneOffset.UTC)
            );

            firestore.collection("feeds")
                    .document(receiverId)
                    .collection("posts")
                    .document(postResponse.getId().toString())
                    .set(postReferenceMap);
        }
    }

    public void pushCommentToPostOwner(CommentResponse commentResponse) {
        Map<String, Object> commentResponseMap = objectMapper.convertValue(commentResponse, new TypeReference<>() {
        });

        firestore.collection("comments")
                .document(commentResponse.getId().toString())
                .set(commentResponseMap);
    }

    public void pushMessageToReceiver(com.example.demo.models.Message message) {
        MessageResponse messageResponse = new MessageResponse().toDTO(message);
        Map<String, Object> messageMap = objectMapper.convertValue(messageResponse, new TypeReference<>() {
        });

        String conversationId = message.getConversation().getId().toString();
        if (!message.getConversation().getIsPending()) {
            firestore.collection("conversations")
                    .document(conversationId)
                    .collection("messages")
                    .document(messageResponse.getId().toString())
                    .set(messageMap);
        } else {
            firestore.collection("conversations")
                    .document(conversationId)
                    .collection("pending_messages")
                    .document(messageResponse.getId().toString())
                    .set(messageMap);
        }
    }

    public void pushApprovedMessage(com.example.demo.models.Message message) {
        MessageResponse messageResponse = new MessageResponse().toDTO(message);
        Map<String, Object> messageMap = objectMapper.convertValue(messageResponse, new TypeReference<>() {
        });

        String conversationId = message.getConversation().getId().toString();

        firestore.collection("conversations")
                .document(conversationId)
                .collection("messages")
                .document(message.getId().toString())
                .set(messageMap);

        firestore.collection("conversations")
                .document(conversationId)
                .collection("pending_messages")
                .document(message.getId().toString())
                .delete();
    }

    public void deletePendingMessages(com.example.demo.models.Message message) {
        String conversationId = message.getConversation().getId().toString();
        firestore.collection("conversations")
                .document(conversationId)
                .collection("pending_messages")
                .document(message.getId().toString())
                .delete();
    }

    public Blob uploadFile(MultipartFile file) {
        try {
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();

            Bucket bucket = StorageClient.getInstance(firebaseApp).bucket(bucketName);

            Blob blob = bucket.create(fileName, file.getBytes(), file.getContentType());

            blob.createAcl(Acl.of(Acl.User.ofAllUsers(), Acl.Role.READER));
            return blob;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public void deleteFile(String url) {
        try {
            String[] parts = url.split("/");
            String fileNameWithParams = parts[parts.length - 1];
            String fileName = fileNameWithParams.split("\\?")[0];
            Bucket bucket = StorageClient.getInstance(firebaseApp).bucket(bucketName);
            Blob blob = bucket.get(fileName);
            if (blob != null) {
                boolean deleted = blob.delete();
                if (deleted) {
                    System.out.println("File deleted successfully: " + fileName);
                } else {
                    System.out.println("Failed to delete the file: " + fileName);
                }
            } else {
                System.out.println("File not found in the bucket: " + fileName);
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }


    public void pushGroupMessageToMembers(com.example.demo.models.Message savedMessage) {
        MessageResponse messageResponse = new MessageResponse().toDTO(savedMessage);
        Map<String, Object> messageMap = objectMapper.convertValue(messageResponse, new TypeReference<Map<String, Object>>() {
        });

        String groupId = savedMessage.getChatGroup().getId().toString();
        firestore.collection("chat_groups")
                .document(groupId)
                .collection("messages")
                .document(messageResponse.getId().toString())
                .set(messageMap);
    }

    public void pushNotificationToUser(Notification notification, User user) {
        String groupId = notification.getGroup() != null ? notification.getGroup().getId().toString() : "";

        Map<String, Object> notificationData = Map.of(
                "message", notification.getMessage(),
                "createdAt", notification.getCreatedAt().toEpochSecond(ZoneOffset.UTC),
                "type", notification.getType().name(),
                "actionUrl", notification.getActionUrl(),
                "sender", notification.getSender().getId(),
                "group", groupId
        );

        firestore.collection("notifications")
                .document(user.getId())
                .collection("user_notifications")
                .add(notificationData);
    }
}
