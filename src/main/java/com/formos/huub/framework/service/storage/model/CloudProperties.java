/**
 * ***************************************************
 * * Description :
 * * File        : CloudProperty
 * * Author      : Hung Tran
 * * Date        : Jan 13, 2023
 * ***************************************************
 **/
package com.formos.huub.framework.service.storage.model;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "cloud")
public class CloudProperties {

    private String provider;
    private String region;
    private Credentials credentials;
    private Bucket bucket;
    private String cloudFrontUrl;

    private PreSigned preSigned;
    private Stack stack;

    @Data
    public static class Credentials {

        private String accessKey;
        private String secretKey;
    }

    @Data
    public static class Bucket {

        private String name;
    }

    @Data
    public static class PreSigned {

        private int urlExpiration;
    }

    @Data
    public static class Stack {

        private boolean auto;
    }
}
