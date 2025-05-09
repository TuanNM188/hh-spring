package com.formos.huub.domain.response.activecampaign;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseContactTags {

    private ContactTags contact;

    @Getter
    @Setter
    public static class ContactTags {
        private String cdate;
        private String contact;
        private String id;
        private Links links;
        private String tag;
    }

    @Getter
    @Setter
    public static class Links {
        private String contact;
        private String tag;
    }

}
