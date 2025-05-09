package com.formos.huub.domain.request.communityboarduserrestrict;

import com.formos.huub.framework.validation.constraints.RequireCheck;
import com.formos.huub.framework.validation.constraints.UUIDCheck;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestAddRestrictMember {

    @RequireCheck
    @UUIDCheck
    private String userId;

    @UUIDCheck
    private String portalId;
}
