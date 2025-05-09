package com.formos.huub.domain.response.directmessage;

import com.formos.huub.domain.enums.ConversationStatusEnum;
import com.formos.huub.domain.enums.ConversationTypeEnum;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class ResponseDetailConversation {

    private UUID id;

    private String name;

    private String imageUrl;

    private ConversationTypeEnum conversationType;

    private ConversationStatusEnum conversationStatus;

    private Instant createdDate;

    private Boolean isBlocked;

    private UUID blockerId;

    private Boolean isArchived;

    private List<ResponseConversationUser> conversationUsers;

    private Boolean isNew;

    private String directUserName;

    private String directUserImageUrl;

    private Boolean isRequireResponseReferralMessage;

    private UUID portalId;

    private String portalName;

    private String portalPrimaryColor;

}
