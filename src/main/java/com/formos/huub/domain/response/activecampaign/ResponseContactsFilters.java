package com.formos.huub.domain.response.activecampaign;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ResponseContactsFilters {

    private List<ResponseContact> contacts;
    private Meta meta;

    @Getter
    @Setter
    public static class Meta {
        private String total;
        private Boolean sortable;
    }
}
