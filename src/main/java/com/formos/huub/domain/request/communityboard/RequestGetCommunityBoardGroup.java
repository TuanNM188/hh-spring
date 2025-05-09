package com.formos.huub.domain.request.communityboard;

import com.formos.huub.framework.validation.constraints.UUIDCheck;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestGetCommunityBoardGroup {

    @UUIDCheck
    private String portalId;

    private String searchKeyword;

    private Integer page;

    private Integer size;

    private String sort;
}
