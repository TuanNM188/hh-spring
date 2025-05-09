package com.formos.huub.repository;

import com.formos.huub.domain.entity.CommunityBoardGroupSetting;
import com.formos.huub.domain.entity.embedkey.CommunityBoardGroupSettingEmbedKey;
import com.formos.huub.domain.enums.SettingCategoryEnum;
import com.formos.huub.domain.enums.SettingKeyCodeEnum;
import com.formos.huub.domain.response.communityboard.IResponseCommunityBoardGroupSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CommunityBoardGroupSettingRepository extends JpaRepository<CommunityBoardGroupSetting, CommunityBoardGroupSettingEmbedKey> {

    @Query(
        value = "SELECT sd.id AS id, sd.keyCode AS settingKey, COALESCE(cbs.settingValue, sd.defaultValue) AS settingValue, " +
            "sd.dataType AS dataType, sd.category AS category, sd.priorityOrder AS priorityOrder, sd.options as options, " +
            "sd.isDisable AS isDisable, sd.title AS title, sd.groupCode AS groupCode, sd.groupName AS groupName, sd.description AS description  " +
            "FROM SettingDefinition sd " +
            "LEFT JOIN CommunityBoardGroupSetting cbs on cbs.id.settingDefinition.id = sd.id AND cbs.id.communityBoardGroup.id = :communityBoardGroupId " +
            "WHERE sd.category = (:category) "
    )
    List<IResponseCommunityBoardGroupSetting> findAllByCommunityBoardGroupIdAndCategory(UUID communityBoardGroupId, SettingCategoryEnum category);

    @Query(
        value = "SELECT sd.id AS id, sd.keyCode AS settingKey, sd.defaultValue AS settingValue, " +
            "sd.dataType AS dataType, sd.category AS category, sd.priorityOrder AS priorityOrder, sd.options as options, " +
            "sd.isDisable AS isDisable, sd.title AS title, sd.groupCode AS groupCode, sd.groupName AS groupName, sd.description AS description  " +
            "FROM SettingDefinition sd " +
            "WHERE sd.category = (:category) "
    )
    List<IResponseCommunityBoardGroupSetting> findAllByCommunityBoardGroupSettings(SettingCategoryEnum category);

    @Query("""
        SELECT cbgs.settingValue
        FROM CommunityBoardGroupSetting cbgs
        WHERE cbgs.id.communityBoardGroup.id = :groupId
            and cbgs.settingKey = :settingKey
    """)
    Optional<String> findByGroupIdAndSettingKey(UUID groupId, SettingKeyCodeEnum settingKey);
}
