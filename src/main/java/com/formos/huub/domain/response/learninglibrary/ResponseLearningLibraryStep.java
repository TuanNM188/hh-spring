package com.formos.huub.domain.response.learninglibrary;

import com.formos.huub.domain.enums.LearningStatusEnum;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class ResponseLearningLibraryStep {

    private UUID id;

    private String name;

    private Integer priorityOrder;

    private LearningStatusEnum learningStatus;

    private List<ResponseLearningLibraryLesson> learningLibraryLessons;

}
