package com.formos.huub.domain.response.blockeduser;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

public interface IResponseBlockedUser {

    UUID getBlockerId();

    UUID getBlockedId();

    String getBlockedName();

    Instant getBlockedDate();

    UUID getConversationId();
}
