package com.formos.huub.domain.request.communityboardlike;

import com.formos.huub.framework.validation.constraints.RequireCheck;
import com.formos.huub.framework.validation.constraints.UUIDCheck;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestAddLikeCommunityBoardComment {

    @RequireCheck
    @UUIDCheck
    private String commentId;

    private String likeIcon;
}
