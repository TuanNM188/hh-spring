package com.formos.huub.domain.response.technicaladvisor;

import java.util.UUID;

public interface IResponseRecommendAdvisor {

    UUID getUserId();

    UUID getTechnicalAdvisorId();

    String getFullName();

    String getImageUrl();

    String getCategories();

}
