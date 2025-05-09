package com.formos.huub.domain.response.member;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponsePortalByRole {

    private UUID id;

    private String platformName;

    private String portalUrl;
}
