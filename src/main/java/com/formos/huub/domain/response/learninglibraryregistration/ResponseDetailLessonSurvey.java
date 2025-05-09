package com.formos.huub.domain.response.learninglibraryregistration;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseDetailLessonSurvey {

    private UUID id;

    private String businessOwnerName;

    private String phoneNumber;

    private String email;

    private String businessName;

    private String courseName;

    private String lessonName;

    private Instant submissionDate;

    private String surveyJson;

    private String surveyData;

    private UUID userId;

}
