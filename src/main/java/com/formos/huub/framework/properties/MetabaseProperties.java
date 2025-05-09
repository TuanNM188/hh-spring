package com.formos.huub.framework.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "metabase")
public class MetabaseProperties {

    private String siteUrl;

    private String secretKey;
}
