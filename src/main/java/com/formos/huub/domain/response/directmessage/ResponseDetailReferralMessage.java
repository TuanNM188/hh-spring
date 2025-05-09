package com.formos.huub.domain.response.directmessage;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
public class ResponseDetailReferralMessage {

    private UUID id;

    private String message;

    private Boolean isResponse;

    private String messageResponse;

    private Instant sendAt;

    private Instant responseAt;

    private ResponseUserBasic businessOwner;


    private ResponseUserBasic primaryUser;

    private UUID communityPartnerId;

    private String communityPartnerName;
}
