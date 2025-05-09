package com.formos.huub.domain.request.communityboardpost;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
public class RequestGetListCommunityBoardPost {

    private UUID groupId;

    private UUID portalId;

    private UUID userId;

    private String searchKeyword;
    private String mentionUser;
    private Instant currentTime = Instant.now();
}
