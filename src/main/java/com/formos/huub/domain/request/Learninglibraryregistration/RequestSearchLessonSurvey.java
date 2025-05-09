package com.formos.huub.domain.request.Learninglibraryregistration;

import com.formos.huub.domain.request.common.SearchConditionRequest;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class RequestSearchLessonSurvey extends SearchConditionRequest {

    private String searchKeyword;

    private UUID portalId;
}
