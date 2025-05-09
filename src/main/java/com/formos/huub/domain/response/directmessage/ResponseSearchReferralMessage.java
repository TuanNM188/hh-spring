package com.formos.huub.domain.response.directmessage;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
public class ResponseSearchReferralMessage {

    private UUID id;

    private UUID conversationId;

    private UUID communityPartnerId;

    private String communityPartnerName;

    private Instant sendAt;

    private String message;

    private String responseMessage;
}
