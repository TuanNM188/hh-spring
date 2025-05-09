package com.formos.huub.domain.response.portals;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponsePortalLocation {

    private  ResponseLocation country;

    private List<ResponsePortalState> states;
}
