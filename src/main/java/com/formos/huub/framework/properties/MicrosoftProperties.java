package com.formos.huub.framework.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "microsoft")
public class MicrosoftProperties {

    private String clientId;

    private String clientSecret;

    private String redirectUri;

    private String authority;

    private String scopes;
}
