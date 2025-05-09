package com.formos.huub.domain.response.survey;

import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResponseSurveyResponses {

    private UUID id;

    private Instant submissionDate;

    private String fullName;

    private String role;

    private String surveyData;

    private String pdfUrl;

    private ResponseSurvey survey;
}
