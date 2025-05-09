package com.formos.huub.domain.entity;

import com.formos.huub.domain.entity.embedkey.UserSettingEmbedKey;
import com.formos.huub.domain.enums.SettingKeyCodeEnum;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "user_setting")
public class UserSetting {

    @EmbeddedId
    private UserSettingEmbedKey id;

    @Enumerated(EnumType.STRING)
    @Column(name = "setting_key")
    private SettingKeyCodeEnum settingKey;

    @Column(name = "setting_value")
    private String settingValue;
}
