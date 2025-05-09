package com.formos.huub.config;

import com.formos.huub.framework.properties.FirebaseProperties;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.FileInputStream;

@Component
@Slf4j
public class FirebaseConfiguration {
    private final ResourceLoader resourceLoader;


    private final FirebaseProperties firebaseProperties;

    @Autowired
    public FirebaseConfiguration(ResourceLoader resourceLoader, FirebaseProperties firebaseProperties
    ) {
        this.resourceLoader = resourceLoader;
        this.firebaseProperties = firebaseProperties;
    }

    @PostConstruct
    public void initializeFirebase() {
        try {
            Resource resource = resourceLoader.getResource(firebaseProperties.getServiceAccountPath());
            String serviceAccount = resource.getFile().getAbsolutePath();
            FirebaseOptions options = new FirebaseOptions.Builder()
                .setCredentials(GoogleCredentials.fromStream(new FileInputStream(serviceAccount)))
                .setDatabaseUrl(firebaseProperties.getRealTimeDatabaseUrl())
                .build();
            FirebaseApp.initializeApp(options);
            log.info("Firebase initialized successfully!");
        } catch (Exception e) {
            log.error("Failed to initialize Firebase: {}", e.getMessage());
        }
    }

}
