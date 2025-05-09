package com.formos.huub.domain.request.communityboardpost;

import com.formos.huub.domain.enums.CommunityBoardVisibilityEnum;
import com.formos.huub.framework.validation.constraints.EnumCheck;
import com.formos.huub.framework.validation.constraints.RequireCheck;
import com.formos.huub.framework.validation.constraints.UUIDCheck;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class RequestGetListMemberMention {

    @UUIDCheck
    private String groupId;

    private List<String> portalIds;

    @RequireCheck
    @EnumCheck(enumClass = CommunityBoardVisibilityEnum.class)
    private String visibility;

    private String searchKeyword;
}
