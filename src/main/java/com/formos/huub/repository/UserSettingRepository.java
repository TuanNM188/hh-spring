package com.formos.huub.repository;

import com.formos.huub.domain.entity.UserSetting;
import com.formos.huub.domain.entity.embedkey.UserSettingEmbedKey;
import com.formos.huub.domain.enums.SettingCategoryEnum;
import com.formos.huub.domain.response.usersetting.IResponseUserSetting;
import com.formos.huub.domain.response.usersetting.IResponseValueUserSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserSettingRepository extends JpaRepository<UserSetting, UserSettingEmbedKey> {

    @Query(
        value = "SELECT sd.id AS id, sd.keyCode AS settingKey, COALESCE(us.settingValue, sd.defaultValue) AS settingValue, " +
        "sd.dataType AS dataType, sd.category AS category, sd.priorityOrder AS priorityOrder, sd.options as options, " +
        "sd.isDisable AS isDisable, sd.title AS title, sd.groupCode AS groupCode, sd.groupName AS groupName, sd.titleKey as titleKey, sd.groupKey as groupKey   " +
        "FROM SettingDefinition sd " +
        "LEFT JOIN UserSetting us on us.id.settingDefinition.id = sd.id AND us.id.user.id = :userId " +
        "WHERE sd.category = (:category) ORDER BY sd.priorityOrder"
    )
    List<IResponseUserSetting> findAllByUserIdAndCategory(UUID userId, SettingCategoryEnum category);

    @Query(value = "select u.id as userId, u.image_url as imageUrl, us.setting_key as settingKey, u.first_name as name, u.email as email,  " +
        " us.setting_value::jsonb ->> 'email' as isEnableEmail, " +
        " us.setting_value::jsonb ->> 'web' as isEnableWeb " +
        " from user_setting us " +
        " join jhi_user u on us.user_id = u.id " +
        " join setting_definition sd on sd.id = us.setting_definition_id " +
        " where us.setting_key = :keyCode and u.id IN (:userIds) and sd.category = 'NOTIFICATION'", nativeQuery = true)
    List<IResponseValueUserSetting> getValueByKeyCodeAndUserId(@Param("keyCode") String keyCode, @Param("userIds") List<UUID> userIds);

}
