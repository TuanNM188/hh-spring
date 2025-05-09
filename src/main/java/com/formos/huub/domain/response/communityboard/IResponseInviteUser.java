package com.formos.huub.domain.response.communityboard;

import java.time.Instant;
import java.util.UUID;

public interface IResponseInviteUser {

    UUID getId();
    String getImageUrl();
    String getName();
    Instant getTimeRequest();
}
