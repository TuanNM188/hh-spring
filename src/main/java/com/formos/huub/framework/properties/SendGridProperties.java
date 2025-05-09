/**
 * ***************************************************
 * * Description :
 * * File        : SendGridProperties
 * * Author      : Hung Tran
 * * Date        : Dec 17, 2024
 * ***************************************************
 **/
package com.formos.huub.framework.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "sendgrid")
public class SendGridProperties {

    private String key;
    private String sender;
    private String displayName;
}
