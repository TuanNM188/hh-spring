/**
 * ***************************************************
 * * Description :
 * * File        : RequestUserSetting
 * * Author      : Hung Tran
 * * Date        : Oct 17, 2024
 * ***************************************************
 **/
package com.formos.huub.domain.request.usersetting;

import java.util.UUID;

import com.formos.huub.domain.enums.SettingCategoryEnum;
import com.formos.huub.domain.enums.SettingKeyCodeEnum;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestUserSetting {

    private UUID id;

    private SettingKeyCodeEnum settingKey;

    private String settingValue;

    private SettingCategoryEnum category;

    private String dataType;
}
