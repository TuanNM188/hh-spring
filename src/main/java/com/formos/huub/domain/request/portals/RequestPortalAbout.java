package com.formos.huub.domain.request.portals;

import com.formos.huub.domain.enums.PortalStatusEnum;
import com.formos.huub.framework.validation.constraints.RequireCheck;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RequestPortalAbout {

    @RequireCheck
    private String platformName;

    private Boolean isCustomDomain;

    @RequireCheck
    private String portalUrl;

    @NotNull
    private PortalStatusEnum status;

    private String organization;

    private String address1;

    private String address2;

    private String country;

    private String city;

    private String state;

    private String zipCode;

    private String primaryContactName;

    @Email
    private String primaryContactEmail;

    private String primaryContactPhone;

    private String primaryExtension;

    private Boolean isSameAsPrimary;

    private String billingName;

    @Email
    private String billingEmail;

    private String billingPhone;

    private String billingExtension;

    private String aboutPageContent;

}
