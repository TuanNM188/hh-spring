package com.formos.huub.domain.request.directmessage;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RequestSearchConversation {

    private String searchKeyword;

    private UUID conversationId;

    private String directUserId;

    private String status;

    private String conversationType;

    private Boolean isArchived;

    private UUID portalId;

    private String conversationMessageType;
}
