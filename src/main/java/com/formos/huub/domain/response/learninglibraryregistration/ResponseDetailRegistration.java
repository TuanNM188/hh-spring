package com.formos.huub.domain.response.learninglibraryregistration;

import com.formos.huub.domain.enums.AccessTypeEnum;
import com.formos.huub.domain.enums.RegistrationStatusEnum;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseDetailRegistration {

    private UUID id;

    private String businessOwnerName;

    private String phoneNumber;

    private String email;

    private String businessName;

    private String courseName;

    private RegistrationStatusEnum registrationStatus;

    private Instant decisionDate;

    private Integer enrolleeLimit;

    private Integer numberOfRegistered;

    private Instant registrationDate;

    private Instant enrollmentDeadline;

    private Instant startDate;

    private Instant endDate;

    private String surveyJson;

    private String surveyData;

    private Boolean isRegistrationFormRequired;

    private UUID userId;

    private AccessTypeEnum courseType;

}
