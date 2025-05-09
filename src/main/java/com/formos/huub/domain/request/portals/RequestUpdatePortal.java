package com.formos.huub.domain.request.portals;

import com.formos.huub.framework.validation.constraints.UUIDCheck;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestUpdatePortal extends RequestBasePortal {

    @UUIDCheck
    private String id;
}
