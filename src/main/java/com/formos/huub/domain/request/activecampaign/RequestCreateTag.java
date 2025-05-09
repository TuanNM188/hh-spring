package com.formos.huub.domain.request.activecampaign;

import lombok.*;

@Getter
@Setter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class RequestCreateTag {

    private RequestTag tag;

    @Getter
    @Setter
    @Builder
    @RequiredArgsConstructor
    @AllArgsConstructor
    public static class RequestTag {
        private String tag;
        private String tagType;
        private String description;
    }

}
