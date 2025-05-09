package com.formos.huub.domain.request.Learninglibraryregistration;

import com.formos.huub.domain.enums.RegistrationStatusEnum;
import com.formos.huub.framework.validation.constraints.RequireCheck;
import com.formos.huub.framework.validation.constraints.UUIDCheck;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class RequestReviewRegistration {

    @UUIDCheck
    private String registrationId;

    @NotNull
    private RegistrationStatusEnum registrationStatus;

}
