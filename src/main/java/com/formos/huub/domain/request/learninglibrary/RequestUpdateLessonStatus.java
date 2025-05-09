package com.formos.huub.domain.request.learninglibrary;

import com.formos.huub.domain.enums.LearningStatusEnum;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class RequestUpdateLessonStatus {

    private UUID learningLibraryId;

    private UUID lessonId;

    private LearningStatusEnum learningStatus;

}
