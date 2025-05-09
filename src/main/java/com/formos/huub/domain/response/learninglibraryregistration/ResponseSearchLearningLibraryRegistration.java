package com.formos.huub.domain.response.learninglibraryregistration;

import com.formos.huub.domain.enums.RegistrationStatusEnum;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class ResponseSearchLearningLibraryRegistration {

    private UUID id;

    private String businessOwnerName;

    private String courseName;

    private Instant submissionDate;

    private RegistrationStatusEnum status;

    private String portalName;
}
