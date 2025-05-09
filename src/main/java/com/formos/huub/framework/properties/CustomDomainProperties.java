/**
 * ***************************************************
 * * Description :
 * * File        : CustomDomainProperties
 * * Author      : Hung Tran
 * * Date        : Nov 11, 2024
 * ***************************************************
 **/
package com.formos.huub.framework.properties;

import java.util.List;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "custom-domain")
public class CustomDomainProperties {

    private List<String> primaryDomain;

    private Boolean treatWwwAsSubdomain;
}
