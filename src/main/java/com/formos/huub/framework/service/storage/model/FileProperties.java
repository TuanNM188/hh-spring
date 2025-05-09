/**
 * ***************************************************
 * * Description :
 * * File        : FileProperties
 * * Author      : Hung Tran
 * * Date        : Jan 09, 2023
 * ***************************************************
 **/
package com.formos.huub.framework.service.storage.model;

import com.formos.huub.framework.constant.AppConstants;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "file")
public class FileProperties {

    private Long maxSize;

    private Long avatarMaxSize;

    private HUUBPath mac;

    private HUUBPath linux;

    private HUUBPath windows;

    public HUUBPath getPath() {
        String os = System.getProperty("os.name");
        if (os.toLowerCase().startsWith(AppConstants.WIN)) {
            return windows;
        } else if (os.toLowerCase().startsWith(AppConstants.MAC)) {
            return mac;
        }
        return linux;
    }

    @Data
    public static class HUUBPath {

        private String path;

        private String avatar;
    }
}
