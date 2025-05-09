package com.formos.huub.domain.response.directmessage;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class BlockedUserInfo {
    private UUID blockerId;
}
