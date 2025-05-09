package com.formos.huub.domain.response.eventregistration;

import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
public class ResponseSearchEventRegistration {

    private UUID id;

    private Instant registrationDate;

    private String subject;

    private Instant startTime;

    private Instant endTime;

    private String businessOwnerName;

    private String portalName;
}
