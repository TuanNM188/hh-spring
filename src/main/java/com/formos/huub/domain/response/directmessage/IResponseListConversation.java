package com.formos.huub.domain.response.directmessage;


import com.formos.huub.domain.enums.ConversationStatusEnum;
import com.formos.huub.domain.enums.ConversationTypeEnum;

import java.time.Instant;
import java.util.UUID;

public interface IResponseListConversation {

    UUID getId();
    String getName();
    String getUserInConversation();
    ConversationTypeEnum getConversationType();
    ConversationStatusEnum getStatus();
    Instant getCreatedDate();
    Boolean getIsArchived();
    String getAuthorities();
    UUID getPortalId();
    String getPortalName();
    String getPortalPrimaryColor();
}
