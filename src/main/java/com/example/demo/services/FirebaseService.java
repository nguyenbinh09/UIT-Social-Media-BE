package com.example.demo.services;

import com.example.demo.dtos.responses.CommentResponse;
import com.example.demo.dtos.responses.MessageResponse;
import com.example.demo.dtos.responses.PostResponse;
import com.example.demo.models.Comment;
import com.example.demo.models.Post;
import com.example.demo.repositories.UserRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.storage.Acl;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.firebase.FirebaseApp;
import com.google.firebase.cloud.StorageClient;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class FirebaseService {
    private final DatabaseReference firebaseDatabase;
    private final FirebaseApp firebaseApp;
    private final String bucketName;
    private final ObjectMapper objectMapper;
    private final NotificationService notificationService;

    @Autowired
    public FirebaseService(@Value("${firebase.bucket.name}") String bucketName, DatabaseReference firebaseDatabase, FirebaseApp firebaseApp, ObjectMapper objectMapper, NotificationService notificationService) {
        this.firebaseDatabase = firebaseDatabase;
        this.firebaseApp = firebaseApp;
        this.bucketName = bucketName;
        this.objectMapper = objectMapper;
        this.notificationService = notificationService;
    }

    public void pushPostToReceivers(Post post, List<String> receiverIds) {
        PostResponse postResponse = new PostResponse().toDTO(post);
        Map<String, Object> postResponseMap = objectMapper.convertValue(postResponse, new TypeReference<Map<String, Object>>() {
        });
        firebaseDatabase.child("posts").child(postResponse.getId().toString())
                .setValueAsync(postResponseMap);
        for (String receiverId : receiverIds) {
            Map<String, Object> postReferenceMap = Map.of(
                    "post_id", post.getId(),
                    "title", postResponse.getTitle(),
                    "timestamp", postResponse.getCreatedAt().toEpochSecond(ZoneOffset.UTC)
            );
            // Push post notification to each receiver in Firebase
            firebaseDatabase.child("feeds").child(receiverId).child(postResponse.getId().toString())
                    .setValueAsync(postReferenceMap);
        }
    }

    public void sendNotificationToUser(String fcmToken, String title, String message) {
        try {
            Message firebaseMessage = Message.builder()
                    .setToken(fcmToken)
                    .putData("title", title)
                    .putData("message", message)
                    .build();
            FirebaseMessaging.getInstance(firebaseApp).send(firebaseMessage);
        } catch (FirebaseMessagingException e) {
            throw new RuntimeException("Failed to send FCM notification", e);
        }
    }

    public void pushCommentToPostOwner(Comment comment) {
        CommentResponse commentResponse = new CommentResponse().toDTO(comment);
        Map<String, Object> commentResponseMap = objectMapper.convertValue(commentResponse, new TypeReference<Map<String, Object>>() {
        });

        firebaseDatabase.child("comments")
                .child(commentResponse.getId().toString())
                .setValueAsync(commentResponseMap);
    }

    public void pushMessageToReceiver(com.example.demo.models.Message message) {
        MessageResponse messageResponse = new MessageResponse().toDTO(message);
        Map<String, Object> messageMap = objectMapper.convertValue(messageResponse, new TypeReference<Map<String, Object>>() {
        });

        String conversationId = message.getConversation().getId().toString();
        if (!message.getConversation().getIsPending()) {
            firebaseDatabase.child("conversations").child(conversationId).child("messages")
                    .child(messageResponse.getId().toString())
                    .setValueAsync(messageMap);
        } else {
            firebaseDatabase.child("conversations").child(conversationId).child("pending_messages")
                    .child(messageResponse.getId().toString())
                    .setValueAsync(messageMap);
        }
    }

    public void pushApprovedMessage(com.example.demo.models.Message message) {
        Map<String, Object> messageMap = objectMapper.convertValue(message, new TypeReference<Map<String, Object>>() {
        });

        firebaseDatabase.child("conversations")
                .child(message.getConversation().getId().toString())
                .child("messages")
                .child(message.getId().toString())
                .setValueAsync(messageMap);

        firebaseDatabase.child("conversations")
                .child(message.getConversation().getId().toString())
                .child("pending_messages")
                .child(message.getId().toString())
                .removeValueAsync();
    }

    public void deletePendingMessages(Long conversationId) {
        firebaseDatabase.child("conversations")
                .child(conversationId.toString())
                .child("pending_messages")
                .removeValueAsync();
    }

    public String uploadFile(MultipartFile file) {
        try {
            String fileName = file.getOriginalFilename() + "_" + System.currentTimeMillis();

            Bucket bucket = StorageClient.getInstance(firebaseApp).bucket(bucketName);

            Blob blob = bucket.create(fileName, file.getBytes(), file.getContentType());

            blob.createAcl(Acl.of(Acl.User.ofAllUsers(), Acl.Role.READER));
            return blob.getMediaLink();
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
}
