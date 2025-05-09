package com.formos.huub.domain.response.report;

import java.util.UUID;

public interface IResponseSearchReport {
    UUID getId();
    UUID getPortalId();
    String getPlatformName();
    Integer getMonth();
    Integer getYear();
    String getSummary();
    String getPdfUrl();

}
