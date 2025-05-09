package com.formos.huub.framework.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "google")
public class GoogleProperties {

    private String clientId;

    private String clientSecret;

    private String calendarId;

    private String redirectUri;
}
