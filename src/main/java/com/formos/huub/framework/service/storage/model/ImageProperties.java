package com.formos.huub.framework.service.storage.model;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "image")
public class ImageProperties {

    private int sizeOfAvatar;
}
