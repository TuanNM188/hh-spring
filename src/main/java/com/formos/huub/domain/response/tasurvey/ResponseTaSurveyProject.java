package com.formos.huub.domain.response.tasurvey;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ResponseTaSurveyProject {
    private UUID projectId;
    private UUID businessOwnerId;
    private UUID advisorId;
    private UUID vendorId;
    private String businessOwnerName;
    private String businessOwnerEmail;
    private String advisorName;
    private String vendorName;
    private String projectName;
    private Integer rating;
    private String feedback;
}
