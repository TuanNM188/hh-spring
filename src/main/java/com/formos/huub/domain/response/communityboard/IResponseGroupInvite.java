package com.formos.huub.domain.response.communityboard;

import java.time.Instant;
import java.util.UUID;

public interface IResponseGroupInvite {
    UUID getGroupId();
    String getGroupName();
    UUID getInviteId();
    String getInviteName();
    Instant getCreateDate();
    UUID getUserId();
}
