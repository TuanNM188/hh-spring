package com.formos.huub.domain.request.communityboardcomment;

import com.formos.huub.framework.validation.constraints.RequireCheck;
import com.formos.huub.framework.validation.constraints.UUIDCheck;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestGetListMemberMention {

    @RequireCheck
    @UUIDCheck
    private String postId;

    private String searchKeyword;
}
