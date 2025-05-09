package com.formos.huub.domain.response.businessowner;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResponseCheckLocationSupported {

    private Boolean isSupported;

    private String portalSupportedUrl;

    private String transferKey;

    private String newPortalName;
}
