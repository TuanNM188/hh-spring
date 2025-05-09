package com.formos.huub.domain.request.directmessage;

import com.formos.huub.domain.enums.MessageTypeEnum;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class RequestSendMessageAll {

    private String message;

    private MessageTypeEnum messageType;

    private List<UUID> portalIds;

}
