package com.example.demo.configs;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;

@Configuration
public class FirebaseConfig {
    @Value("${firebase.database.url}")
    private String databaseUrl;
    @Value("${firebase.bucket.name}")
    private String bucketName;
    @Value("${firebase.key.base64}")
    private String firebaseKeyBase64;

    @Bean
    public FirebaseApp initializeFirebaseApp() throws IOException {
        byte[] decodedKey = Base64.getDecoder().decode(firebaseKeyBase64);
        ByteArrayInputStream serviceAccount = new ByteArrayInputStream(decodedKey);

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .setDatabaseUrl(databaseUrl)
                .build();

        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseApp.initializeApp(options);
        }
        return FirebaseApp.getInstance();
    }

    @Bean
    public DatabaseReference firebaseDatabaseReference(FirebaseApp firebaseApp) {
        // Ensure we have an initialized FirebaseApp instance before accessing DatabaseReference
        return FirebaseDatabase.getInstance(firebaseApp).getReference();
    }
}
