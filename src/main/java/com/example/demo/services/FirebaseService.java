package com.example.demo.services;

import com.example.demo.dtos.responses.PostResponse;
import com.example.demo.models.Post;
import com.google.cloud.storage.Acl;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.firebase.FirebaseApp;
import com.google.firebase.cloud.StorageClient;
import com.google.firebase.database.DatabaseReference;
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

    @Autowired
    public FirebaseService(@Value("${firebase.bucket.name}") String bucketName, DatabaseReference firebaseDatabase, FirebaseApp firebaseApp) {
        this.firebaseDatabase = firebaseDatabase;
        this.firebaseApp = firebaseApp;
        this.bucketName = bucketName;
    }

    public void pushPostToFollowers(Post post, List<String> followerIds) {
        PostResponse postResponse = new PostResponse().toDTO(post);
        for (String followerId : followerIds) {
            // Push post notification to each follower in Firebase
            firebaseDatabase.child("feeds").child(followerId).child(postResponse.getId().toString())
                    .setValueAsync(Map.of(
                            "user_id", postResponse.getUser().getId(),
                            "title", postResponse.getTitle(),
                            "content", postResponse.getTextContent(),
                            "timestamp", postResponse.getCreatedAt().toEpochSecond(ZoneOffset.UTC)
                    ));
        }
    }

    public String uploadFile(MultipartFile file) {
        try {
            Bucket bucket = StorageClient.getInstance(firebaseApp).bucket(bucketName);

            Blob blob = bucket.create(file.getOriginalFilename(), file.getBytes(), file.getContentType());

            blob.createAcl(Acl.of(Acl.User.ofAllUsers(), Acl.Role.READER));
            return blob.getMediaLink();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
