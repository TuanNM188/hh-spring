package com.formos.huub.domain.response.learninglibrary;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseLearningLibraryDetail {

    private UUID id;

    private ResponseLearningLibraryAbout about;

    private ResponseLearningLibraryRegistration registration;

    private List<UUID> speakers;

    private List<ResponseLearningLibraryStep> learningLibrarySteps;
}
