package com.formos.huub.domain.request.businessowner;

import com.formos.huub.domain.request.common.SearchConditionRequest;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestSearchEventRegistrations extends SearchConditionRequest {
    private UUID portalId;
    private String searchKeyword;
}
