package com.formos.huub.domain.request.learninglibrary;

import jakarta.validation.Valid;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class RequestCreateLearningLibrary {

    @Valid
    private RequestLearningLibraryAbout about;

    private RequestLearningLibraryRegistration registration;

    private List<UUID> speakers;

    private @Valid List<RequestLearningLibraryStep> learningLibrarySteps;

    private Boolean isDraft;

}
