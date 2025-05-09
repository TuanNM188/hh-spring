package com.formos.huub.domain.request.communityboardpost;

import com.formos.huub.domain.enums.CommunityBoardTargetTypeEnum;
import com.formos.huub.framework.validation.constraints.EnumCheck;
import com.formos.huub.framework.validation.constraints.RequireCheck;
import com.formos.huub.framework.validation.constraints.UUIDCheck;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestMemberFlagContent {

    @UUIDCheck
    @RequireCheck
    private String targetId;

    @EnumCheck(enumClass = CommunityBoardTargetTypeEnum.class)
    @RequireCheck
    private String targetType;

    @RequireCheck
    private String reason;

}
