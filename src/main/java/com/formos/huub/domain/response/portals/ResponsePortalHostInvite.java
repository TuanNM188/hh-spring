package com.formos.huub.domain.response.portals;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponsePortalHostInvite {

    private UUID id;

    private String email;

    private String firstName;

    private String lastName;

    private Boolean isExist;

    private String inviteToken;

}
