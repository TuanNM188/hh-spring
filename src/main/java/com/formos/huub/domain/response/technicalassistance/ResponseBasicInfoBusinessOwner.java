package com.formos.huub.domain.response.technicalassistance;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class   ResponseBasicInfoBusinessOwner {

    private UUID id;

    private String normalizedFullName;

    private String imageUrl;

    private String email;

    private String phoneNumber;

    private String country;

    private String state;

    private String city;

    private String address;

    private String zipCode;

    private String nameOfYourBusiness;

    private String preferToBeContacted;
}
