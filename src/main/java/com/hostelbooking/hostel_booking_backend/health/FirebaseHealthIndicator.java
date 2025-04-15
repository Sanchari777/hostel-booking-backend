package com.hostelbooking.hostel_booking_backend.health;

import com.google.firebase.FirebaseApp;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class FirebaseHealthIndicator implements HealthIndicator {
    @Override
    public Health health() {
        try {
            FirebaseApp.getInstance();
            return Health.up()
                    .withDetail("service", "Firebase")
                    .withDetail("status", "Initialized")
                    .build();
        } catch (IllegalStateException e) {
            return Health.down()
                    .withDetail("service", "Firebase")
                    .withDetail("error", "Firebase not initialized")
                    .build();
        }
    }
}