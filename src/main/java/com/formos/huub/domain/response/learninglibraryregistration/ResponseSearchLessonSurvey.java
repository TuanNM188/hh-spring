package com.formos.huub.domain.response.learninglibraryregistration;

import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ResponseSearchLessonSurvey {

    private UUID id;

    private String businessOwnerName;

    private String courseName;

    private String lessonName;

    private Instant submissionDate;

    private String surveyData;

    private String surveyName;

    private String surveyDescription;

    private String portalName;
}
