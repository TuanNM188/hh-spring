package com.formos.huub.domain.request.portals;

import com.formos.huub.framework.validation.constraints.RequireCheck;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class RequestPortalWelcomeMessage {

    @NotNull
    private UUID userSendMessageId;

    @RequireCheck
    private String welcomeMessage;

}
