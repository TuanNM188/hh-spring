package com.formos.huub.domain.response.activecampaign;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseTag {

    private Tag tag;

    @Getter
    @Setter
    public static class Tag {
        private String tag;
        private String description;
        private String tagType;
        private String cdate;
        private Links links;
        private String id;
    }

    @Getter
    @Setter
    public static class Links {
        private String contactGoalTags;
    }

}
