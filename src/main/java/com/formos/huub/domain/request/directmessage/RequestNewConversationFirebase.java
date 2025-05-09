package com.formos.huub.domain.request.directmessage;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RequestNewConversationFirebase {

    private List<RequestParticipant> participants;

    private Long lastUpdated;

    private String conversationId;

    private String modifyType;
}
