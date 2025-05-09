package com.formos.huub.domain.request.authenticate;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class RequestSignInAsUser {

    private String userName;

    private UUID portalId;
}
