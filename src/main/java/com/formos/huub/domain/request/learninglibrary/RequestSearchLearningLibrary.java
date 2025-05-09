package com.formos.huub.domain.request.learninglibrary;

import com.formos.huub.domain.request.common.SearchConditionRequest;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class RequestSearchLearningLibrary extends SearchConditionRequest {

    private UUID portalId;
}
