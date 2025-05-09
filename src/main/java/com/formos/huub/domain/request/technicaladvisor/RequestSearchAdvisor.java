package com.formos.huub.domain.request.technicaladvisor;


import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Setter
@Getter
public class RequestSearchAdvisor {

    private UUID portalId;

    private String searchKeyword;

    private String languages;

    private String services;

    private String categories;

    private String specialties;

    private String areas;

    private String communityPartnerIds;

    private Integer page;

    private Integer size;

    private String sort;

    private UUID technicalAssistanceId;

    private Boolean isCurrentApplication;

    private List<UUID> excludeUserIds;
}
