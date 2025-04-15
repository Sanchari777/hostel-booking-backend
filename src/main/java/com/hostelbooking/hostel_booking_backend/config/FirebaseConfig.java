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
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Configuration
public class FirebaseConfig {

    @Bean
    public Firestore firestore() throws Exception {
        if (FirebaseApp.getApps().isEmpty()) {
            InputStream serviceAccount;

            // 1️ If running in Heroku (env variable exists)
            String firebaseEnv = System.getenv("GOOGLE_CREDENTIALS");
            if (firebaseEnv != null && !firebaseEnv.trim().isEmpty()) {
                System.out.println(" Using Firebase config from HEROKU environment");
                serviceAccount = new ByteArrayInputStream(firebaseEnv.getBytes(StandardCharsets.UTF_8));
            }
            // 2️ Local fallback (read from JSON file)
            else {
                System.out.println(" Using Firebase config from local JSON file");
                serviceAccount = new FileInputStream("src/main/resources/hostel-booking-6a210-firebase-adminsdk-fbsvc-85c0e30ab3.json");
            }

            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            FirebaseApp.initializeApp(options);
        }

        return FirestoreClient.getFirestore();
    }
}