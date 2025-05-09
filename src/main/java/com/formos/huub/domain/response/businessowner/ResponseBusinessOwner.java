package com.formos.huub.domain.response.businessowner;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseBusinessOwner {

    private UUID userId;

    private String firstName;

    private String lastName;

    private String portalName;
}
