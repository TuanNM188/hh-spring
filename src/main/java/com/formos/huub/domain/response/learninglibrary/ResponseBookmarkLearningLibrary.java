package com.formos.huub.domain.response.learninglibrary;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ResponseBookmarkLearningLibrary {

    private UUID learningLibraryId;

    private Boolean isBookmark;

}
