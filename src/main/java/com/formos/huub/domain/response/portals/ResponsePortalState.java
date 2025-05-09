package com.formos.huub.domain.response.portals;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponsePortalState {

    private UUID id;

    private String countryCode;

    private String countryName;

    private ResponseLocation state;

    private List<ResponseLocation> cities;

    private List<ResponseLocation> zipcodes;

    private Integer priorityOrder;
}
