package com.formos.huub.domain.request.member;

import com.formos.huub.domain.request.common.SearchConditionRequest;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestSearchMemberConnection extends SearchConditionRequest {
    private String searchKeyword;

    private String role;
}
