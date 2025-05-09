package com.formos.huub.domain.request.businessowner;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class RequestCheckPortalSupportedLocation {

    private UUID portalId;

    private String city;

    private String state;

    private String zipCode;

    private Object data;

    private String countryCode;

    private String country;

    private String stateName;

}
