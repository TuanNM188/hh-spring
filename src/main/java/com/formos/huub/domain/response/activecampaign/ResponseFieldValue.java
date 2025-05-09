package com.formos.huub.domain.response.activecampaign;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResponseFieldValue {
    private String contact;
    private String field;
    private String value;
    private String cdate;
}
