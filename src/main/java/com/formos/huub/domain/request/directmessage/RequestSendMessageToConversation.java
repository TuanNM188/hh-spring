package com.formos.huub.domain.request.directmessage;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class RequestSendMessageToConversation {

    private List<UUID> userIds;

    private String message;

    private UUID senderId;

    private UUID communityPartnerId;

}
