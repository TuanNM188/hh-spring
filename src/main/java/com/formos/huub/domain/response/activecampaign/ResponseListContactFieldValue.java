package com.formos.huub.domain.response.activecampaign;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ResponseListContactFieldValue {
    private List<ResponseFieldValue> fieldValues;

    @Getter
    @Setter
    public static class ResponseFieldValue {
        private String contact;
        private String field;
        private String value;
        private String cdate;
        private String udate;
        private String created_by;
        private String updated_by;
        private String id;
        private String owner;
    }
}
