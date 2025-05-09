package com.formos.huub.domain.response.usersetting;

import com.formos.huub.domain.enums.SettingKeyCodeEnum;

import java.util.UUID;

public interface IResponseValueUserSetting {

    UUID getUserId();

    SettingKeyCodeEnum getSettingKey();

    Boolean getIsEnableEmail();

    Boolean getIsEnableWeb();

    String getName();
    String getEmail();
    String getImageUrl();
}
