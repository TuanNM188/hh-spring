package com.formos.huub.domain.request.communityboard;

import com.formos.huub.domain.enums.SettingCategoryEnum;
import com.formos.huub.domain.enums.SettingKeyCodeEnum;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
public class RequestCommunityBoardGroupSetting {

    private UUID id;

    private SettingKeyCodeEnum settingKey;

    private String settingValue;

    private SettingCategoryEnum category;

    private String dataType;

}
