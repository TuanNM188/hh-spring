package com.formos.huub.domain.request.learninglibrary;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class RequestRatingLearningLibrary {

    private UUID learningLibraryId;

    private Integer rating;

}
