/**
 * ***************************************************
 * * Description :
 * * File        : PortalValidator
 * * Author      : Hung Tran
 * * Date        : Nov 12, 2024
 * ***************************************************
 **/
package com.formos.huub.validator;

import static com.formos.huub.framework.constant.AppConstants.PORTAL_URL_REGEX;
import static com.formos.huub.framework.constant.AppConstants.SUB_DOMAIN_REGEX;

import com.formos.huub.domain.constant.BusinessConstant;
import com.formos.huub.domain.entity.Portal;
import com.formos.huub.domain.request.portals.*;
import com.formos.huub.framework.exception.BadRequestException;
import com.formos.huub.framework.message.MessageHelper;
import com.formos.huub.framework.message.model.Message;
import com.formos.huub.framework.utils.ObjectUtils;
import com.formos.huub.helper.portal.PortalHelper;
import com.formos.huub.repository.PortalRepository;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PortalValidator {

    private final PortalRepository portalRepository;

    /**
     * validate Portal Host
     *
     * @param request List<RequestPortalHost>
     */
    public void validatePortalHost(List<RequestPortalHost> request) {
        if (ObjectUtils.isEmpty(request)) {
            return;
        }
        var primaryMembers = request.stream().filter(ele -> Objects.nonNull(ele.getIsPrimary()) && ele.getIsPrimary()).toList();
        if (ObjectUtils.isEmpty(primaryMembers) || primaryMembers.size() > BusinessConstant.NUMBER_1) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0015, "Primary Portal Host"));
        }
    }

    public boolean checkExistPlatformName(String requestPlatformName, String oldPlatformName) {
        if (oldPlatformName != null) {
            return isPlatformNameChanged(requestPlatformName, oldPlatformName)
                && isPlatformNameTaken(requestPlatformName);
        }
        return isPlatformNameTaken(requestPlatformName);
    }

    private boolean isPlatformNameChanged(String requestPlatformName, String oldPlatformName) {
        return !oldPlatformName.equals(requestPlatformName);
    }

    private boolean isPlatformNameTaken(String platformName) {
        return portalRepository.existsByPlatformName(platformName);
    }

    /**
     * Validate Create Portal
     *
     * @param request RequestCreatePortal
     * @param origin  String
     */
    public void validateCreate(RequestCreatePortal request, String origin) {
        var portalAbout = request.getPortalAbout();
        validatePortalDomain(origin, portalAbout);
        validatePlatformNameCreate(portalAbout.getPlatformName());
        validateSubdomainCreate(portalAbout);
    }

    /**
     * Validate Update Portal
     *
     * @param id      UUID
     * @param portal  Portal
     * @param request RequestUpdatePortal
     * @param origin  String
     */
    public void validateUpdate(UUID id, Portal portal, RequestUpdatePortal request, String origin) {
        RequestPortalAbout portalAbout = request.getPortalAbout();
        validatePortalDomain(origin, portalAbout);
        validatePlatformNameUpdate(portal.getPlatformName(), portalAbout.getPlatformName());
        validateSubdomainUpdate(id, portalAbout);
    }

    /**
     * validate Contract Dates
     *
     * @param request RequestPortalProgram
     */
    public void validateContractDates(RequestPortalProgram request) {
        if (Objects.isNull(request.getContractStart()) || request.getContractStart().isAfter(request.getContractEnd())) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0015, "Contract start"));
        }
    }

    /**
     * Validate Portal Domain
     *
     * @param origin  String
     * @param request RequestPortalAbout
     */
    private void validatePortalDomain(String origin, RequestPortalAbout request) {
        if (ObjectUtils.isEmpty(request.getPortalUrl())) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0027, "Portal Url"));
        }
        String invalidMessage = Boolean.TRUE.equals(request.getIsCustomDomain()) ? "domain" : "subdomain";
        String pattern = Boolean.TRUE.equals(request.getIsCustomDomain()) ? PORTAL_URL_REGEX : SUB_DOMAIN_REGEX;
        var isUrlValid = Pattern.compile(pattern).matcher(request.getPortalUrl()).matches();
        if (!isUrlValid || isExistOrigin(origin, request.getPortalUrl())) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0047, invalidMessage));
        }
    }

    private boolean isExistOrigin(String origin, String url){
        if (ObjectUtils.isEmpty(origin)){
            return false;
        }
        String normalizedDomain = origin.replaceAll("^http?://", "");
        return url.endsWith(normalizedDomain);
    }

    /**
     * Validate Platform Name Update
     */
    private void validatePlatformNameUpdate(String oldPlatformName, String requestPlatformName) {
        if (!oldPlatformName.equals(requestPlatformName) && isPlatformNameTaken(requestPlatformName)) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0017, "Platform Name"));
        }
    }

    /**
     * Validate Platform Name Create
     */
    private void validatePlatformNameCreate(String requestPlatformName) {
        if (isPlatformNameTaken(requestPlatformName)) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0017, "Platform Name"));
        }
    }

    /**
     * Validate Subdomain Update
     *
     * @param id      UUID
     * @param request RequestPortalAbout
     */
    private void validateSubdomainUpdate(UUID id, RequestPortalAbout request) {
        var isExistSubdomain = portalRepository.existsBySubdomainAndNotEqualId(request.getPortalUrl(), id);
        if (Boolean.TRUE.equals(isExistSubdomain)) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0017, "subdomain"));
        }
    }

    /**
     * Validate Subdomain Create
     *
     * @param request RequestPortalAbout
     */
    private void validateSubdomainCreate(RequestPortalAbout request) {
        var isExistSubdomain = portalRepository.existsBySubdomain(request.getPortalUrl());
        if (Boolean.TRUE.equals(isExistSubdomain)) {
            throw new BadRequestException(MessageHelper.getMessage(Message.Keys.E0017, "subdomain"));
        }
    }


}
