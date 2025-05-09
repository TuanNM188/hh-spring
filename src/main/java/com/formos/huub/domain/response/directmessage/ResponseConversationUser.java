package com.formos.huub.domain.response.directmessage;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ResponseConversationUser {

    private UUID userId;

    private String name;

    private String imageUrl;

    private  String authorities;
}
