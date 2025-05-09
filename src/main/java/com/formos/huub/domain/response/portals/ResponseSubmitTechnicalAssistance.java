package com.formos.huub.domain.response.portals;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseSubmitTechnicalAssistance {

    private UUID userId;

    private String firstName;

    private String lastName;

    private String portalName;

    private String programManagerFirstName;

    private String programManagerEmail;
}
