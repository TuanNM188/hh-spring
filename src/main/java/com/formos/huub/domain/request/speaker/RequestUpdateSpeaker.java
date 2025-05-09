package com.formos.huub.domain.request.speaker;

import com.formos.huub.framework.validation.constraints.RequireCheck;
import com.formos.huub.framework.validation.constraints.UUIDCheck;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestUpdateSpeaker {

    @UUIDCheck
    @RequireCheck
    private String id;

    @RequireCheck
    private String firstName;

    @RequireCheck
    private String lastName;

    @RequireCheck
    private String bio;

    private String avatar;

}
