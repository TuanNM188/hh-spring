package com.formos.huub.domain.request.member;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RequestGetUserInPortal {
    UUID portalId;
    List<UUID> portalIds;
    String visibility;
    UUID groupId;
    UUID postAuthorId;
    UUID ignoreUserId;
    String searchKeyword;
}
