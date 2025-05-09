package com.formos.huub.domain.request.learninglibrary;

import com.formos.huub.framework.validation.constraints.UUIDCheck;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class RequestUpdateLearningLibrary {

    @UUIDCheck
    private String id;

    @Valid
    private RequestLearningLibraryAbout about;

    private RequestLearningLibraryRegistration registration;

    private List<UUID> speakers;

    private @Valid List<RequestLearningLibraryStep> learningLibrarySteps;

    private Boolean isDraft;

}
