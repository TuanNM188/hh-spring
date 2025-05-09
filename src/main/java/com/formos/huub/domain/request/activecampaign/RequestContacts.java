package com.formos.huub.domain.request.activecampaign;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class RequestContacts {

    private String email;

    private String firstName;

    private String lastName;

    private String phone;

    private List<FieldValues> fieldValues;

    @Getter
    @Setter
    public static class FieldValues {
        public FieldValues(String field, String value) {
            this.field = field;
            this.value = value;
        }
        private String field;
        private String value;
    }
}
