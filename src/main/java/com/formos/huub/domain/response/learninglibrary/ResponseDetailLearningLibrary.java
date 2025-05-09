package com.formos.huub.domain.response.learninglibrary;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class ResponseDetailLearningLibrary {

    private UUID id;

    private String name;

    private Instant lastActivityDate;

    private Long progress;

    private List<ResponseLearningLibraryStep> learningLibrarySteps;
}
