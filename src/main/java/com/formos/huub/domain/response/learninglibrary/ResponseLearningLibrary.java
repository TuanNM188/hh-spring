package com.formos.huub.domain.response.learninglibrary;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class ResponseLearningLibrary {

    private UUID id;

    private List<ResponseLearningLibraryStep> learningLibrarySteps;

}
