package com.formos.huub.domain.response.businessowner;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseInfoBusinessOwner {

    private UUID id;

    private UUID businessOwnerId;

    private String fullName;

    private String firstName;

    private String lastName;

    private String imageUrl;

    private String username;

    private String platformName;

    private UUID portalId;

    private String businessName;


}
