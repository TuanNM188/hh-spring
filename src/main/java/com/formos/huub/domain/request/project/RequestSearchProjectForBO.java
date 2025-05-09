package com.formos.huub.domain.request.project;

import com.formos.huub.domain.request.common.SearchConditionRequest;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class RequestSearchProjectForBO extends SearchConditionRequest {

    private UUID userId;
}
