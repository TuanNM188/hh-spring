package com.formos.huub.domain.request.location;

import com.formos.huub.domain.request.portals.RequestLocation;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RequestCreateLocation {

    private List<RequestLocation> locations;

}
