package com.formos.huub.domain.request.common;

import com.formos.huub.domain.enums.CombinationTypeEnum;
import com.formos.huub.domain.enums.ConditionTypeEnum;
import com.formos.huub.domain.enums.FilterTypeEnum;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SqlConditionRequest {
    private String column;
    private ConditionTypeEnum conditionType;
    private Object value;
    private FilterTypeEnum filterType;
    private List<String> values;
    private CombinationTypeEnum combinationType;
    private String timezone;
    private List<SqlConditionRequest> subConditions;

    public boolean isGroup() {
        return subConditions != null;
    }
}
