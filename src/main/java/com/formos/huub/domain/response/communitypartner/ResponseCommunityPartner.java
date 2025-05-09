package com.formos.huub.domain.response.communitypartner;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseCommunityPartner {

    private UUID id;

    private String name;

    private String imageUrl;


}
