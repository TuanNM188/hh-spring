package com.formos.huub.domain.request.communityboard;

import com.formos.huub.framework.validation.constraints.UUIDCheck;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestGetListMemberInvite {

    @UUIDCheck
    private String portalId;

    @UUIDCheck
    private String groupId;

    private String searchKeyword;
}
