package com.formos.huub.domain.entity.embedkey;

import com.formos.huub.domain.entity.CommunityBoardGroup;
import com.formos.huub.domain.entity.SettingDefinition;
import jakarta.persistence.Embeddable;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
public class CommunityBoardGroupSettingEmbedKey {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "community_board_group_id", referencedColumnName = "id")
    private CommunityBoardGroup communityBoardGroup;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "setting_definition_id", referencedColumnName = "id")
    private SettingDefinition settingDefinition;
}
