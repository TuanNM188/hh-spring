package com.formos.huub.domain.response.technicalassistance;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ResponseInfoVendor {

    private UUID vendorId;

    private UUID communityPartnerId;

    private UUID navigatorId;

    private String navigatorName;

    private String imageUrl;

    private String communityPartnerName;

    private Float calculatedHours;

}
