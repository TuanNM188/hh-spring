package com.formos.huub.domain.response.project;

public interface IResponseCountProject {

    Integer getNumProposed();
    Integer getNumVendorApproved();
    Integer getNumWorkInProgress();
    Integer getNumCompleted();
    Integer getNumOverdue();
    Integer getNumInvoiced();
    Integer getNumDenied();

}
