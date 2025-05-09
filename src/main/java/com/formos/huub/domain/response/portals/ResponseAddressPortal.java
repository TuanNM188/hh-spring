package com.formos.huub.domain.response.portals;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ResponseAddressPortal {

    private UUID id;

    private String platformName;

    private String primaryLogo;

    private String address1;
    private String address2;
    private String country;
    private String city;
    private String zipCode;
}
