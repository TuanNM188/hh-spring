package com.formos.huub.domain.request.activecampaign;

import lombok.*;

@Getter
@Setter
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class RequestContactTags {

    private String contact;

    private String tag;

}
