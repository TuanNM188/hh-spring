package com.formos.huub.domain.response.directmessage;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ResponseUserBasic {

    private UUID id;

    private String fullName;

    private String imageUrl;

    private UUID businessOwnerId;

    private String businessName;
}
