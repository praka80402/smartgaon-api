package com.smartgaon.ai.smartgaon_api.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.InputStream;

/**
 * Initializes the Firebase Admin SDK once, at application startup.
 *
 * The service-account location is read from application.properties:
 *
 *   firebase.config.path=classpath:firebase-service-account.json   (local / dev)
 *   firebase.config.path=file:/opt/smartgaon/firebase.json         (server / prod)
 *
 * Because it uses Spring's ResourceLoader, both "classpath:" and "file:" work
 * with no code change — only the property differs between environments.
 */
@Component
public class FirebaseConfig {

    private final ResourceLoader resourceLoader;

    @Value("${firebase.config.path:classpath:firebase-service-account.json}")
    private String firebaseConfigPath;

    public FirebaseConfig(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @PostConstruct
    public void init() {
        try {
            // Don't initialize twice (Spring may re-run in some reload scenarios).
            if (!FirebaseApp.getApps().isEmpty()) {
                return;
            }

            Resource resource = resourceLoader.getResource(firebaseConfigPath);
            try (InputStream serviceAccount = resource.getInputStream()) {
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();
                FirebaseApp.initializeApp(options);
            }

            System.out.println("Firebase Admin initialized from: " + firebaseConfigPath);

        } catch (Exception e) {
            // Fail loud but don't crash the whole app if Firebase can't start —
            // only the /api/auth/firebase endpoint will be affected.
            System.err.println("Firebase init failed: " + e.getMessage());
        }
    }
}