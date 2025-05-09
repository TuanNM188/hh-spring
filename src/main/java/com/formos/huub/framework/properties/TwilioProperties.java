/**
 * ***************************************************
 * * Description :
 * * File        : TwilioProperties
 * * Author      : Huy Truong
 * * Date        : Jan 17, 2025
 * ***************************************************
 **/
package com.formos.huub.framework.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "twilio")
public class TwilioProperties {

    private String accountSid;
    private String authToken;
    private String phoneNumber;
}
