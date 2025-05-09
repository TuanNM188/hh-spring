package com.formos.huub.domain.response.communitypartner;

import com.formos.huub.domain.enums.StatusEnum;
import com.formos.huub.domain.enums.UserStatusEnum;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ResponseSearchCommunityPartner {

    private UUID id;

    private String name;

    private String email;

    private Boolean isVendor;

    private StatusEnum status;

    private String state;

    private String city;
}
