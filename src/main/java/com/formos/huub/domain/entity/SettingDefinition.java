/**
 * ***************************************************
 * * Description :
 * * File        : SettingDefinition
 * * Author      : Hung Tran
 * * Date        : Oct 16, 2024
 * ***************************************************
 **/
package com.formos.huub.domain.entity;

import com.formos.huub.domain.enums.SettingCategoryEnum;
import com.formos.huub.domain.enums.SettingGroupCodeEnum;
import com.formos.huub.domain.enums.SettingKeyCodeEnum;
import jakarta.persistence.*;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "setting_definition")
@Getter
@Setter
public class SettingDefinition {

    @Id
    @GeneratedValue(generator = "UUID")
    @Column(name = "id", length = 36)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SettingCategoryEnum category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private SettingKeyCodeEnum keyCode;

    @Column(nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(name = "group_code")
    private SettingGroupCodeEnum groupCode;

    @Column(name = "group_name")
    private String groupName;

    @Column(name = "data_type")
    private String dataType;

    @Column(name = "default_value")
    private String defaultValue;

    @Column(name = "options")
    private String options;

    @Column(name = "priority_order")
    private Integer priorityOrder;

    @Column(name = "is_disable")
    private Boolean isDisable;

    @Column(name = "description")
    private String description;

    @Column(name = "title_key")
    private String titleKey;

    @Column(name = "group_key")
    private String groupKey;

}
