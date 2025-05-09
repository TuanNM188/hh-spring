package com.formos.huub.domain.response.learninglibrary;

import com.formos.huub.domain.enums.AccessTypeEnum;
import com.formos.huub.domain.enums.RegistrationStatusEnum;
import com.formos.huub.domain.response.speaker.ResponseSpeakerDetail;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class ResponseOverviewLearningLibrary {

    private UUID id;

    private String name;

    private String categoryName;

    private UUID categoryId;

    private String description;

    private AccessTypeEnum accessType;

    private Instant lastModifiedDate;

    private Instant lastActivityDate;

    private String heroImage;

    private Long progress;

    private Integer numLessons;

    private Integer numDownloads;

    private List<ResponseLearningLibraryTag> tags;

    private List<ResponseSpeakerDetail> speakers;

    private List<ResponseLearningLibraryStep> learningLibrarySteps;

    private Boolean isRegistrationFormRequired;

    private String surveyJson;

    private Integer enrolleeLimit;

    private Integer numberOfRegistered;

    private RegistrationStatusEnum registrationStatus;

    private Integer expiresIn;

    private Instant enrollmentDeadline;

    private Instant startDate;

    private Instant endDate;

}
