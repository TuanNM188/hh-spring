package com.formos.huub.domain.request.directmessage;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class RequestMentionedUser {

    private List<UUID> userIds;

    private UUID conversationId;
}
