package com.formos.huub.domain.request.member;

import com.formos.huub.domain.enums.FollowStatusEnum;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class RequestFollowerUser {

    @NotNull
    private UUID userId;

    private FollowStatusEnum status;
}
