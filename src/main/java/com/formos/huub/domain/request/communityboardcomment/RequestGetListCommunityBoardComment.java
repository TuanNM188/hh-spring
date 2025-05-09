package com.formos.huub.domain.request.communityboardcomment;

import com.formos.huub.framework.validation.constraints.RequireCheck;
import com.formos.huub.framework.validation.constraints.UUIDCheck;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RequestGetListCommunityBoardComment {

    @UUIDCheck
    @RequireCheck
    private String postId;

    @UUIDCheck
    private String parentId;

    private String ignoreCommentIds;
}
