package com.formos.huub.domain.response.directmessage;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.formos.huub.domain.enums.ConversationStatusEnum;
import com.formos.huub.domain.enums.ConversationTypeEnum;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class ResponseConversation {

    private UUID id;

    private String name;

    private String imageUrl;

    private ConversationStatusEnum status;

    private ConversationTypeEnum conversationType;

    private Boolean isBlocked;

    private Boolean isArchived;

    private Instant createdDate;

    private UUID portalId;

    private String portalName;

    private String portalPrimaryColor;

    @JsonIgnore
    private List<ResponseConversationUser> conversationUsers;

}
