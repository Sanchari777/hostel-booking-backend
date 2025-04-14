package com.hostelbooking.hostel_booking_backend.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Base64;

@Configuration
public class FirebaseConfig {

    @Bean
    public Firestore firestore() throws Exception {
        if (FirebaseApp.getApps().isEmpty()) {
            String base64;
            String env = System.getenv("FIREBASE_CONFIG");

            if (env != null && !env.trim().isEmpty()) {
                base64 = env.trim();
                System.out.println("✅ Using FIREBASE_CONFIG from environment");
            } else {
                InputStream is = new ClassPathResource("firebase_encoded.txt").getInputStream();
                byte[] encodedBytes = is.readAllBytes();
                base64 = new String(encodedBytes).replaceAll("\\s+", "");
                System.out.println("✅ Using firebase_encoded.txt from resources");
            }

            try {
                byte[] decodedBytes = Base64.getDecoder().decode(base64);
                InputStream serviceAccount = new ByteArrayInputStream(decodedBytes);

                FirebaseOptions options = new FirebaseOptions.Builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();

                FirebaseApp.initializeApp(options);
            } catch (IllegalArgumentException e) {
                System.err.println("🚫 Base64 decoding failed: " + e.getMessage());
                throw new RuntimeException("Base64 decoding failed", e);
            }
        }

        return FirestoreClient.getFirestore();
    }
}