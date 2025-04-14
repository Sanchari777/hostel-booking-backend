package com.hostelbooking.hostel_booking_backend.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;

@Configuration
public class FirebaseConfig {

    @Bean
    public Firestore firestore() throws IOException {
        // Check if FirebaseApp is already initialized to avoid duplicate initialization
        if (FirebaseApp.getApps().isEmpty()) {
            InputStream serviceAccount = new ClassPathResource("hostel-booking-6a210-firebase-adminsdk-fbsvc-1f37a02139.json").getInputStream();

            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            FirebaseApp.initializeApp(options);
        }

        // Return Firestore instance
        return FirestoreClient.getFirestore();
    }
}