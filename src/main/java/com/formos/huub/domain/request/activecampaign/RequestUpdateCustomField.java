package com.formos.huub.domain.request.activecampaign;

import lombok.*;

@Getter
@Setter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class RequestUpdateCustomField {

    private FieldValue fieldValue;

    private Boolean useDefaults;

    @Getter
    @Setter
    @Builder
    @RequiredArgsConstructor
    @AllArgsConstructor
    public static class FieldValue {
        private String contact;
        private String field;
        private String value;
    }

}
