package com.formos.huub.domain.response.technicaladvisor;

import java.util.UUID;

public interface IResponseTechnicalAdvisorInfo {

    UUID getTechnicalAdvisorId();

    String getFullName();

    String getImageUrl();

    UUID getUserId();
}
