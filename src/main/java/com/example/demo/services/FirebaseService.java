package com.example.demo.services;

import com.example.demo.models.Post;
import com.google.firebase.database.DatabaseReference;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class FirebaseService {
    private final DatabaseReference firebaseDatabase;

    public void pushPostToFollowers(Post post, List<String> followerIds) {
        for (String followerId : followerIds) {
            // Push post notification to each follower in Firebase
            firebaseDatabase.child("feeds").child(followerId).child(post.getId().toString())
                    .setValueAsync(Map.of(
                            "user_id", post.getUser().getId(),
                            "title", post.getTitle(),
                            "content", post.getTextContent(),
                            "timestamp", post.getCreatedAt().toEpochSecond(ZoneOffset.UTC)
                    ));
        }
    }
}
