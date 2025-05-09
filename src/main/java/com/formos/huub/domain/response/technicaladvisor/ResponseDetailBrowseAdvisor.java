package com.formos.huub.domain.response.technicaladvisor;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.formos.huub.domain.enums.ApprovalStatusEnum;
import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class ResponseDetailBrowseAdvisor {

    private UUID userId;

    private UUID technicalAdvisorId;

    @JsonProperty("fullName")
    private String normalizedFullName;

    private String imageUrl;

    private String bio;

    private String personalWebsite;

    private String tiktokProfile;

    private String linkedInProfile;

    private String instagramProfile;

    private String facebookProfile;

    private String twitterProfile;

    private ApprovalStatusEnum apply11Status;

    private Float assignAwardHours;

    private Float remainingAwardHours;

    private UUID communityPartnerId;

    private UUID programTermId;

    private List<String> languages;

    private List<String> categories;

    private List<String> services;

    private List<String> specialties;

    private List<String> areas;
}
