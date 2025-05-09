package com.formos.huub.domain.request.portals;

import com.formos.huub.domain.enums.PortalFundingStatusEnum;
import com.formos.huub.domain.enums.PortalFundingTypeEnum;
import com.formos.huub.framework.validation.constraints.EnumCheck;
import com.formos.huub.framework.validation.constraints.RequireCheck;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Setter
@Getter
public class RequestPortalFundingAbout {

    @RequireCheck
    private String title;

    @RequireCheck
    @EnumCheck(enumClass = PortalFundingStatusEnum.class)
    private String status;

    @RequireCheck
    private String dateAdded;

    @RequireCheck
    private String publishDate;

    @RequireCheck
    @EnumCheck(enumClass = PortalFundingTypeEnum.class)
    private String type;

    @RequireCheck
    private String amount;

    @RequireCheck
    private String description;

    private String imageUrl;

    private List<String> fundingCategories;

    private List<UUID> portalIds;


}
