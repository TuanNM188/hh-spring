package com.formos.huub.domain.response.activecampaign;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseTags {

    private List<Tag> tags;

    @Getter
    @Setter
    public static class Tag {
        private String id;
        private String tag;
        private String tagType;
        private String description;
    }

}
