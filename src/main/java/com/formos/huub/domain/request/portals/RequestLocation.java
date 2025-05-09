package com.formos.huub.domain.request.portals;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestLocation {

    private String geoNameId;

    private String name;

    private String code;

    private String type;
}
