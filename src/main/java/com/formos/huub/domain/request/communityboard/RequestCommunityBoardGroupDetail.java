package com.formos.huub.domain.request.communityboard;

import com.formos.huub.framework.validation.constraints.RequireCheck;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RequestCommunityBoardGroupDetail {

    @RequireCheck
    private String groupName;

    @RequireCheck
    private String description;

}
