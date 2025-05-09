package com.formos.huub.domain.response.communityboard;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ResponseCommunityBoardGroupSetting {

    private UUID id;

    private String settingKey;

    private String settingValue;

    private String dataType;

    private String category;

    private Integer priorityOrder;

    private String options;

    private String title;

    private String groupCode;

    private String groupName;

    private String description;

}
