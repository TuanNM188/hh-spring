package com.formos.huub.domain.request.specialty;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RequestUpdateSpecialtyForTA {

    private List<RequestSpecialtyForTA> specialties;
}
