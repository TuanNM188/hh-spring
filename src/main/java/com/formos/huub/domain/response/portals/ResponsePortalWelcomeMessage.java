package com.formos.huub.domain.response.portals;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponsePortalWelcomeMessage {

    private UUID userSendMessageId;

    private String welcomeMessage;

}
