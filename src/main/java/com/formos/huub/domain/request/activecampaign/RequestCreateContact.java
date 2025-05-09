package com.formos.huub.domain.request.activecampaign;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RequestCreateContact {

    private RequestContacts contact;
}
