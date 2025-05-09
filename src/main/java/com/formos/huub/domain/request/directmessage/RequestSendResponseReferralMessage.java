package com.formos.huub.domain.request.directmessage;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class RequestSendResponseReferralMessage {

    private UUID conversationId;

    private String message;

}
