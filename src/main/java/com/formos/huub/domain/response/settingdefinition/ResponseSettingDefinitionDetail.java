/**
 * ***************************************************
 * * Description :
 * * File        : ResponseSettingDefinitionDetail
 * * Author      : Hung Tran
 * * Date        : Oct 17, 2024
 * ***************************************************
 **/
package com.formos.huub.domain.response.settingdefinition;

import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResponseSettingDefinitionDetail {

    private UUID id;

    private String category;

    private String keyCode;

    private String title;

    private String groupCode;

    private String groupName;

    private String dataType;

    private String defaultValue;

    private String options;

    private Integer priorityOrder;

    private Boolean isDisable;
}
