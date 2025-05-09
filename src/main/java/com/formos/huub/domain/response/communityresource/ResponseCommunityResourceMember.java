package com.formos.huub.domain.response.communityresource;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseCommunityResourceMember {

    private UUID id;

    private String firstName;

    private String lastName;

    private String imageUrl;

    private String role;
}
