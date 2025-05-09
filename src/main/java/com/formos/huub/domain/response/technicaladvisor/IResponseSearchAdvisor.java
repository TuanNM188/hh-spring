package com.formos.huub.domain.response.technicaladvisor;

import java.util.UUID;

public interface IResponseSearchAdvisor {

    UUID getUserId();

    UUID getTechnicalAdvisorId();

    String getFullName();

    String getImageUrl();

    String getBio();

    String getCategories();

    String getLanguages();

    String getPersonalWebsite();
}
