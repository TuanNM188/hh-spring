package com.formos.huub.domain.response.technicalassistance;

public interface IResponseCountApplication {

    Integer getNumSubmitted();

    Integer getNumVendorAssigned();

    Integer getNumActive();

    Integer getNumDenied();

    Integer getNumExpired();
}
