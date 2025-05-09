package com.formos.huub.domain.request.businessowner;

import com.formos.huub.framework.context.PortalContextHolder;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RequestSearchBusinessOwner {
    private String searchKeyword;
    private UUID portalId;
    private String applicationStatus;
    private UUID communityPartnerId;
    private UUID technicalAdvisorId;
}
