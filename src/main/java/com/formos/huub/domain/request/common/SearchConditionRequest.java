package com.formos.huub.domain.request.common;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SearchConditionRequest {
    private List<SqlConditionRequest> searchConditions;
    private Integer page;
    private Integer size;
    private String sort;
    private String timezone;
}
