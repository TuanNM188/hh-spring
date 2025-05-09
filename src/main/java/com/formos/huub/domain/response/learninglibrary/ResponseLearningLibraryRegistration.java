package com.formos.huub.domain.response.learninglibrary;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseLearningLibraryRegistration {

    private Boolean isRegistrationFormRequired;

    private String surveyJson;

}
