package com.formos.huub.domain.response.speaker;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
public class ResponseSpeakerDetail {

    private UUID id;

    private String firstName;

    private String lastName;

    private String bio;

    private String avatar;

}
