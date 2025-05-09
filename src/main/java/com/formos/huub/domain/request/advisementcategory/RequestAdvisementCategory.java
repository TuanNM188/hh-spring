package com.formos.huub.domain.request.advisementcategory;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Setter
@Getter
public class RequestAdvisementCategory {

    private UUID id;

    private Integer yearsOfExperience;

    private List<String> serviceIds;

}
