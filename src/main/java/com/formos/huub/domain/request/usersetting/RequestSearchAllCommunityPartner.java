package com.formos.huub.domain.request.usersetting;

import com.formos.huub.domain.enums.StatusEnum;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class RequestSearchAllCommunityPartner {

    private StatusEnum status;

    private Boolean isVendor;

    private UUID portalId;

    private List<UUID> includes;
}
