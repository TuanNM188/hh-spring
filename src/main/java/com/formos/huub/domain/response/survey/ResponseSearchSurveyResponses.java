package com.formos.huub.domain.response.survey;

import java.time.Instant;
import java.util.UUID;
import lombok.Data;

@Data
public class ResponseSearchSurveyResponses {

    private UUID id;

    private Instant submissionDate;

    private String fullName;

    private String role;

    private String pdfUrl;

    private String surveyName;
}
