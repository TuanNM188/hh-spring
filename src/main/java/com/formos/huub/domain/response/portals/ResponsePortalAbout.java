package com.formos.huub.domain.response.portals;

import com.formos.huub.domain.enums.PortalStatusEnum;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResponsePortalAbout {

    String platformName;

    PortalStatusEnum status;

    Boolean isCustomDomain;

    String portalUrl;

    String organization;

    String address1;

    String address2;

    String country;

    String city;

    String state;

    String zipCode;

    String primaryContactName;

    String primaryContactEmail;

    String primaryContactPhone;

    String primaryExtension;

    Boolean isSameAsPrimary;

    String billingName;

    String billingEmail;

    String billingPhone;

    String billingExtension;

    String aboutPageContent;

}
