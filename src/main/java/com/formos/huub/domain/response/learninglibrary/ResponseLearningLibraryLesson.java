package com.formos.huub.domain.response.learninglibrary;

import com.formos.huub.domain.enums.LearningStatusEnum;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class ResponseLearningLibraryLesson {

    private UUID id;

    private String title;

    private Integer priorityOrder;

    private LearningStatusEnum learningStatus;

    private String surveyData;

    private List<ResponseLearningLibrarySection> sections;

}
