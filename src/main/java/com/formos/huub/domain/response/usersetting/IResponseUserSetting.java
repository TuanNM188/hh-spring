/**
 * ***************************************************
 * * Description :
 * * File        : IResponseUserSetting
 * * Author      : Hung Tran
 * * Date        : Oct 17, 2024
 * ***************************************************
 **/
package com.formos.huub.domain.response.usersetting;

import java.util.UUID;

public interface IResponseUserSetting {
    UUID getId();

    String getSettingKey();

    String getSettingValue();

    String getDataType();

    String getCategory();

    Integer getPriorityOrder();

    String getOptions();

    Boolean getIsDisable();

    String getTitle();

    String getGroupCode();

    String getGroupName();

    String getTitleKey();
    String getGroupKey();


}
