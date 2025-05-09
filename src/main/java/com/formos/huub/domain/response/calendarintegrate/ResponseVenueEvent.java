package com.formos.huub.domain.response.calendarintegrate;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResponseVenueEvent {

    private String id;

    private String name;

    private Address address;

    @Getter
    @Setter
    public static class Address {

        @JsonProperty("address_1")
        private String address1;

        @JsonProperty("address_2")
        private String address2;

        private String city;

        private String region;

        @JsonProperty("postal_code")
        private String postalCode;

        @JsonProperty("localized_address_display")
        private String localizedAddressDisplay;

        @JsonProperty("localized_area_display")
        private String localizedAreaDisplay;


    }

}
