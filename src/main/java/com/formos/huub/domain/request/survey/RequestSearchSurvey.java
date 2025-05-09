package com.formos.huub.domain.request.survey;

import com.formos.huub.domain.request.common.SearchConditionRequest;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestSearchSurvey extends SearchConditionRequest {

    private UUID portalId;
}
