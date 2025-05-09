package com.formos.huub.domain.request.learninglibrary;

import lombok.*;

@Getter
@Setter
public class RequestLearningLibraryRegistration {

    private Boolean isRegistrationFormRequired;

    private String surveyJson;

}
