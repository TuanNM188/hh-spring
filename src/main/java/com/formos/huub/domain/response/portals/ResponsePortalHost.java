package com.formos.huub.domain.response.portals;

import com.formos.huub.domain.enums.PortalHostStatusEnum;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
public class ResponsePortalHost {

    private UUID id;

    private String firstName;

    private String lastName;

    private String email;

    private PortalHostStatusEnum status;

    private Boolean isPrimary;

    private Instant createdDate;

    private String fullName;

    private UUID userId;


}
