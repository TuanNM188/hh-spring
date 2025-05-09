package com.formos.huub.helper.file;

import com.formos.huub.framework.constant.AppConstants;
import com.formos.huub.framework.service.storage.model.CloudProperties;
import com.formos.huub.framework.utils.StringUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FileHelper {

    CloudProperties config;

    public String primaryPortalLogo(String imageUrl) {
        if (StringUtils.isNullOrEmpty(imageUrl)) {
            return String.join("/", config.getCloudFrontUrl(), AppConstants.DEFAULT_PORTAL_LOGO);
        }
        if (imageUrl.startsWith("http")) {
            return imageUrl;
        }
        return String.join("/", config.getCloudFrontUrl(), imageUrl);
    }
}
