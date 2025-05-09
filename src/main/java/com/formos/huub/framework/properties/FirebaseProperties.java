package com.formos.huub.framework.properties;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "firebase")
public class FirebaseProperties {

    private String serviceAccountPath;

    private String realTimeDatabaseUrl;

    private Token token = new Token();

    @Getter
    @Setter
    public static class Token {

        private int maxClaimsSize = 1000; // default value
        private RateLimit rateLimit = new RateLimit();
    }

    @Getter
    @Setter
    public static class RateLimit {

        private int tokens = 100; // default value
    }
}
