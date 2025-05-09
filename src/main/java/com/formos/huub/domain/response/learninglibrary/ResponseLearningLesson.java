package com.formos.huub.domain.response.learninglibrary;

import com.formos.huub.domain.enums.LearningStatusEnum;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ResponseLearningLesson {

    private UUID id;

    private String title;

    private Integer priorityOrder;

    private LearningStatusEnum learningStatus;
}
