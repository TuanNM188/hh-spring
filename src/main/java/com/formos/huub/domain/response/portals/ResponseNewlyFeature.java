package com.formos.huub.domain.response.portals;

import com.formos.huub.domain.enums.PortalFundingTypeEnum;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseNewlyFeature {

    private UUID id;

    private String title;

    private String description;

    private String imageUrl;

    private String categoryIcon;

    private PortalFundingTypeEnum fundingType;
}
