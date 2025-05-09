package com.formos.huub.domain.response.activecampaign;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseCustomFieldContact {

    private List<Contacts> contacts;

    private FieldValue fieldValue;

    @Getter
    @Setter
    public static class Contacts {
        private String cdate;
        private String email;
        private String phone;
        private String firstName;
        private String lastName;
        private String orgid;
    }

    @Getter
    @Setter
    public static class FieldValue {
        private String contact;
        private String field;
        private String value;
        private String cdate;
        private String udate;
    }

}
