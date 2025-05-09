/**
 * ***************************************************
 * * Description :
 * * File        : MemberHelper
 * * Author      : Hung Tran
 * * Date        : Nov 20, 2024
 * ***************************************************
 **/
package com.formos.huub.helper.member;

import com.formos.huub.config.ApplicationProperties;
import com.formos.huub.domain.entity.*;
import com.formos.huub.domain.enums.PortalStatusEnum;
import com.formos.huub.domain.response.member.ResponsePortalByRole;
import com.formos.huub.framework.utils.ObjectUtils;
import com.formos.huub.repository.BusinessOwnerRepository;
import com.formos.huub.repository.PortalHostRepository;
import com.formos.huub.repository.UserRepository;
import com.formos.huub.security.AuthoritiesConstants;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MemberHelper {

    PortalHostRepository portalHostRepository;
    BusinessOwnerRepository businessOwnerRepository;
    ApplicationProperties applicationProperties;
    UserRepository userRepository;

    /**
     * Gets the portals by authority.
     *
     * @param userId    the user ID
     * @param authority the authority
     * @param user      the user
     * @return the portals by authority
     */
    public List<Portal> getPortalsByAuthority(UUID userId, String authority, User user) {
        return switch (authority) {
            case AuthoritiesConstants.PORTAL_HOST -> getPortalHostPortals(userId);
            case AuthoritiesConstants.COMMUNITY_PARTNER -> getCommunityPartnerPortals(user);
            case AuthoritiesConstants.BUSINESS_OWNER -> getBusinessOwnerPortals(userId);
            case AuthoritiesConstants.TECHNICAL_ADVISOR -> getTechnicalAdvisorPortals(user);
            default -> Collections.emptyList();
        };
    }

    /**
     * Converts a list of portals to a list of response portals.
     *
     * @param portals the portals
     * @return the response portals
     */
    public List<ResponsePortalByRole> convertToResponse(List<Portal> portals) {
        return portals
            .stream()
            .filter(portal -> PortalStatusEnum.ACTIVE.equals(portal.getStatus()) && !ObjectUtils.isEmpty(portal.getUrl()))
            .map(this::mapToResponsePortalUrl)
            .toList();
    }

    /**
     * Maps a portal to a response portal URL.
     *
     * @param portal the portal
     * @return the response portal URL
     */
    private ResponsePortalByRole mapToResponsePortalUrl(Portal portal) {
        return ResponsePortalByRole.builder()
            .id(portal.getId())
            .platformName(portal.getPlatformName())
            .portalUrl(buildPortalUrl(portal.getUrl()))
            .build();
    }


    /**
     * Builds a portal URL.
     *
     * @param url the URL
     * @return the portal URL
     */
    private String buildPortalUrl(String url) {
        return String.format(applicationProperties.getClientApp().getBaseUrlPortal(), url);
    }

    /**
     * Extracts a single portal from the source object.
     *
     * @param source       the source object
     * @param portalGetter the function to extract the portal
     * @param <T>          the type of the source object
     * @return the extracted portal
     */
    private <T> List<Portal> extractSinglePortal(T source, Function<T, Portal> portalGetter) {
        return Optional.ofNullable(source).map(portalGetter).map(Collections::singletonList).orElseGet(Collections::emptyList);
    }

    /**
     * Extracts multiple portals from the source object.
     *
     * @param source        the source object
     * @param portalsGetter the function to extract the portals
     * @param <T>           the type of the source object
     * @return the extracted portals
     */
    private <T> List<Portal> extractMultiplePortals(T source, Function<T, Set<Portal>> portalsGetter) {
        return Optional.ofNullable(source)
            .map(portalsGetter)
            .filter(portals -> !ObjectUtils.isEmpty(portals))
            .stream()
            .flatMap(Set::stream)
            .collect(Collectors.toList());
    }

    /**
     * Gets the portals of a portal host.
     *
     * @param userId the user ID
     * @return the portals
     */
    private List<Portal> getPortalHostPortals(UUID userId) {
        return portalHostRepository
            .findByUserId(userId)
            .map(host -> extractSinglePortal(host, PortalHost::getPortal))
            .orElseGet(Collections::emptyList);
    }

    /**
     * Gets the portals of a community partner.
     *
     * @param user the user
     * @return the portals
     */
    private List<Portal> getCommunityPartnerPortals(User user) {
        return extractMultiplePortals(user.getCommunityPartner(), CommunityPartner::getPortals);
    }

    /**
     * Gets the portals of a business owner.
     *
     * @param userId the user ID
     * @return the portals
     */
    private List<Portal> getBusinessOwnerPortals(UUID userId) {
        return businessOwnerRepository
            .findByUserId(userId)
            .map(owner -> extractSinglePortal(owner, BusinessOwner::getPortal))
            .orElseGet(Collections::emptyList);
    }

    /**
     * Gets the portals of a technical advisor.
     *
     * @param user the user
     * @return the portals
     */
    private List<Portal> getTechnicalAdvisorPortals(User user) {
        return extractMultiplePortals(user.getTechnicalAdvisor(), TechnicalAdvisor::getPortals);
    }

    /**
     * Get User by Id
     *
     * @param id UUID
     * @return User
     */
    public User getUserById(UUID id) {
        return userRepository.findById(id).orElse(null);
    }
}
