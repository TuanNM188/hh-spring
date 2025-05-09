package com.formos.huub.domain.request.technicaladvisor;

import com.formos.huub.domain.request.common.SqlConditionRequest;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RequestListTechnicalAdvisor {

    private List<SqlConditionRequest> searchConditions;

    private Integer page;

    private Integer size;

    private String sort;

}
