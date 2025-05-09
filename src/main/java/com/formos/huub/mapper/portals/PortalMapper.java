package com.formos.huub.mapper.portals;

import com.formos.huub.domain.entity.Portal;
import com.formos.huub.domain.request.portals.RequestCreatePortal;
import com.formos.huub.domain.request.portals.RequestUpdatePortal;
import com.formos.huub.domain.response.portals.ResponseAboutPortal;
import com.formos.huub.domain.response.portals.ResponseAddressPortal;
import com.formos.huub.domain.response.portals.ResponsePortalDetail;
import com.formos.huub.domain.response.portals.SearchPortalsResponse;
import com.formos.huub.mapper.categories.CategoryMapper;
import com.formos.huub.mapper.funder.FunderMapper;
import com.formos.huub.mapper.program.ProgramMapper;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = {
    PortalHostMapper.class, ProgramMapper.class
}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PortalMapper {

    SearchPortalsResponse toResponse(Portal portal);

    @Mapping(target = "platformName", source = "request.portalAbout.platformName")
    @Mapping(target = "isCustomDomain", source = "request.portalAbout.isCustomDomain")
    @Mapping(target = "url", expression = "java(trimPortalUrl(request.getPortalAbout().getPortalUrl()))")
    @Mapping(target = "status", source = "request.portalAbout.status")
    @Mapping(target = "organization", source = "request.portalAbout.organization")
    @Mapping(target = "address1", source = "request.portalAbout.address1")
    @Mapping(target = "address2", source = "request.portalAbout.address2")
    @Mapping(target = "country", source = "request.portalAbout.country")
    @Mapping(target = "city", source = "request.portalAbout.city")
    @Mapping(target = "state", source = "request.portalAbout.state")
    @Mapping(target = "zipCode", source = "request.portalAbout.zipCode")
    @Mapping(target = "primaryContactName", source = "request.portalAbout.primaryContactName")
    @Mapping(target = "primaryContactEmail", source = "request.portalAbout.primaryContactEmail")
    @Mapping(target = "primaryContactPhone", source = "request.portalAbout.primaryContactPhone")
    @Mapping(target = "primaryExtension", source = "request.portalAbout.primaryExtension")
    @Mapping(target = "isSameAsPrimary", source = "request.portalAbout.isSameAsPrimary")
    @Mapping(target = "billingName", source = "request.portalAbout.billingName")
    @Mapping(target = "billingEmail", source = "request.portalAbout.billingEmail")
    @Mapping(target = "billingPhone", source = "request.portalAbout.billingPhone")
    @Mapping(target = "billingExtension", source = "request.portalAbout.billingExtension")
    @Mapping(target = "aboutPageContent", source = "request.portalAbout.aboutPageContent")
    @Mapping(target = "primaryColor", source = "request.portalCustomize.primaryColor")
    @Mapping(target = "secondaryColor", source = "request.portalCustomize.secondaryColor")
    @Mapping(target = "primaryLogo", source = "request.portalCustomize.primaryLogo")
    @Mapping(target = "secondaryLogo", source = "request.portalCustomize.secondaryLogo")
    @Mapping(target = "favicon", source = "request.portalCustomize.favicon")
    @Mapping(target = "countryId", source = "request.portalLocation.country.geoNameId")
    @Mapping(target = "portalHosts", ignore = true)
    @Mapping(target = "userSendMessageId", source = "request.portalWelcomeMessage.userSendMessageId")
    @Mapping(target = "welcomeMessage", source = "request.portalWelcomeMessage.welcomeMessage")
    Portal toEntity(RequestCreatePortal request);

    @Mapping(target = "platformName", source = "request.portalAbout.platformName")
    @Mapping(target = "isCustomDomain", source = "request.portalAbout.isCustomDomain")
    @Mapping(target = "url", expression = "java(trimPortalUrl(request.getPortalAbout().getPortalUrl()))")
    @Mapping(target = "status", source = "request.portalAbout.status")
    @Mapping(target = "organization", source = "request.portalAbout.organization")
    @Mapping(target = "address1", source = "request.portalAbout.address1")
    @Mapping(target = "address2", source = "request.portalAbout.address2")
    @Mapping(target = "country", source = "request.portalAbout.country")
    @Mapping(target = "city", source = "request.portalAbout.city")
    @Mapping(target = "state", source = "request.portalAbout.state")
    @Mapping(target = "zipCode", source = "request.portalAbout.zipCode")
    @Mapping(target = "primaryContactName", source = "request.portalAbout.primaryContactName")
    @Mapping(target = "primaryContactEmail", source = "request.portalAbout.primaryContactEmail")
    @Mapping(target = "primaryContactPhone", source = "request.portalAbout.primaryContactPhone")
    @Mapping(target = "primaryExtension", source = "request.portalAbout.primaryExtension")
    @Mapping(target = "isSameAsPrimary", source = "request.portalAbout.isSameAsPrimary")
    @Mapping(target = "billingName", source = "request.portalAbout.billingName")
    @Mapping(target = "billingEmail", source = "request.portalAbout.billingEmail")
    @Mapping(target = "billingPhone", source = "request.portalAbout.billingPhone")
    @Mapping(target = "billingExtension", source = "request.portalAbout.billingExtension")
    @Mapping(target = "aboutPageContent", source = "request.portalAbout.aboutPageContent")
    @Mapping(target = "primaryColor", source = "request.portalCustomize.primaryColor")
    @Mapping(target = "secondaryColor", source = "request.portalCustomize.secondaryColor")
    @Mapping(target = "primaryLogo", source = "request.portalCustomize.primaryLogo")
    @Mapping(target = "secondaryLogo", source = "request.portalCustomize.secondaryLogo")
    @Mapping(target = "favicon", source = "request.portalCustomize.favicon")
    @Mapping(target = "countryId", source = "request.portalLocation.country.geoNameId")
    @Mapping(target = "portalHosts", ignore = true)
    @Mapping(target = "userSendMessageId", source = "request.portalWelcomeMessage.userSendMessageId")
    @Mapping(target = "welcomeMessage", source = "request.portalWelcomeMessage.welcomeMessage")
    Portal partialUpdate(@MappingTarget Portal portal, RequestUpdatePortal request);

    @Mapping(target = "portalAbout.platformName", source = "platformName")
    @Mapping(target = "portalAbout.isCustomDomain", source = "isCustomDomain")
    @Mapping(target = "portalAbout.portalUrl", source = "url")
    @Mapping(target = "portalAbout.status", source = "status")
    @Mapping(target = "portalAbout.organization", source = "organization")
    @Mapping(target = "portalAbout.address1", source = "address1")
    @Mapping(target = "portalAbout.address2", source = "address2")
    @Mapping(target = "portalAbout.country", source = "country")
    @Mapping(target = "portalAbout.city", source = "city")
    @Mapping(target = "portalAbout.state", source = "state")
    @Mapping(target = "portalAbout.zipCode", source = "zipCode")
    @Mapping(target = "portalAbout.primaryContactName", source = "primaryContactName")
    @Mapping(target = "portalAbout.primaryContactEmail", source = "primaryContactEmail")
    @Mapping(target = "portalAbout.primaryContactPhone", source = "primaryContactPhone")
    @Mapping(target = "portalAbout.primaryExtension", source = "primaryExtension")
    @Mapping(target = "portalAbout.isSameAsPrimary", source = "isSameAsPrimary")
    @Mapping(target = "portalAbout.billingName", source = "billingName")
    @Mapping(target = "portalAbout.billingEmail", source = "billingEmail")
    @Mapping(target = "portalAbout.billingPhone", source = "billingPhone")
    @Mapping(target = "portalAbout.billingExtension", source = "billingExtension")
    @Mapping(target = "portalAbout.aboutPageContent", source = "aboutPageContent")
    @Mapping(target = "portalCustomize.primaryColor", source = "primaryColor")
    @Mapping(target = "portalCustomize.secondaryColor", source = "secondaryColor")
    @Mapping(target = "portalCustomize.primaryLogo", source = "primaryLogo")
    @Mapping(target = "portalCustomize.secondaryLogo", source = "secondaryLogo")
    @Mapping(target = "portalCustomize.favicon", source = "favicon")
    @Mapping(target = "portalProgram", source = "program")
    @Mapping(target = "portalWelcomeMessage.userSendMessageId", source = "userSendMessageId")
    @Mapping(target = "portalWelcomeMessage.welcomeMessage", source = "welcomeMessage")
    ResponsePortalDetail toResponseDetail(Portal portal);

    ResponseAboutPortal toResponseAbout(Portal portal);
    ResponseAddressPortal toResponseAddress(Portal portal);

    Portal fromIdToEntity(String id);

    default String trimPortalUrl(String portalUrl) {
        if (portalUrl != null && portalUrl.endsWith("/")) {
            return portalUrl.substring(0, portalUrl.length() - 1);
        }
        return portalUrl;
    }

}
