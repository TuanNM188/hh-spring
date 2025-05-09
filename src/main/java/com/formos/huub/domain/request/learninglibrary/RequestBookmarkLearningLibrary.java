package com.formos.huub.domain.request.learninglibrary;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class RequestBookmarkLearningLibrary {

    private UUID learningLibraryId;

    private Boolean isBookmark;

}
