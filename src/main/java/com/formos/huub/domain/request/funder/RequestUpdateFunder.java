package com.formos.huub.domain.request.funder;

import com.formos.huub.framework.validation.constraints.RequireCheck;
import com.formos.huub.framework.validation.constraints.UUIDCheck;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestUpdateFunder {

    @UUIDCheck
    @RequireCheck
    private String id;

    @RequireCheck
    private String name;

    @RequireCheck
    private String funderUrl;

    @RequireCheck
    private String description;

    private String imageUrl;

}
