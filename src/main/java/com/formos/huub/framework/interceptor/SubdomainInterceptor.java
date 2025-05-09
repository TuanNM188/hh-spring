package com.formos.huub.framework.interceptor;

import com.formos.huub.domain.entity.Portal;
import com.formos.huub.domain.entity.User;
import com.formos.huub.framework.context.PortalContext;
import com.formos.huub.framework.context.PortalContextHolder;
import com.formos.huub.framework.exception.NotFoundException;
import com.formos.huub.framework.message.MessageHelper;
import com.formos.huub.framework.message.model.Message;
import com.formos.huub.framework.support.DomainSupport;
import com.formos.huub.repository.UserRepository;
import com.formos.huub.service.portals.PortalService;
import jakarta.persistence.EntityManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * ***************************************************
 * * Description :
 * * File        : SubdomainInterceptor
 * * Author      : Hung Tran
 * * Date        : Sep 28, 2024
 * ***************************************************
 **/
@Component
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Slf4j
public class SubdomainInterceptor implements HandlerInterceptor {

    private static final String X_SUBDOMAIN_HEADER = "X-Subdomain";
    private static final String PORTAL_NOT_FOUND_MESSAGE = "Portal not found";

    PortalService portalService;
    EntityManager entityManager;
    DomainSupport domainSupport;
    UserRepository userRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String origin = request.getHeader(X_SUBDOMAIN_HEADER);
        log.info("Access with domain: {}", origin);

        if (origin == null) {
            return true;
        }

        try {
            if (domainSupport.isMainDomain(origin)) {
                return handleMainDomain(origin);
            }
            return handleCustomDomain(origin);
        } catch (NotFoundException e) {
            response.sendError(HttpStatus.NOT_FOUND.value(), PORTAL_NOT_FOUND_MESSAGE);
            return false;
        }
    }

    private boolean handleMainDomain(String origin) {
        log.info("Handle main domain:: {}", origin);
        Optional<String> subdomainOpt = domainSupport.extractSubdomain(origin);

        if (subdomainOpt.isEmpty()) {
            setAdminContext();
            return true;
        }

        return setupPortalContext(subdomainOpt.get(), origin, false);
    }

    private boolean handleCustomDomain(String origin) {
        return setupPortalContext(origin, origin, true);
    }

    private boolean setupPortalContext(String domain, String origin, boolean isCustomDomain) {
        log.info("Setup portal context with domain: {}", domain);
        Portal portal = portalService
            .getPortalBySubdomainOrCustomDomain(domain, isCustomDomain)
            .orElseThrow(() -> new NotFoundException(MessageHelper.getMessage(Message.Keys.E0017, "Portal")));

        setPortalContext(portal, origin);
        return true;
    }

    private void setAdminContext() {
        log.info("Set admin context");
        PortalContext context = PortalContext.builder().isAdminAccess(true).build();
        PortalContextHolder.setContext(context);
    }

    private void setPortalContext(Portal portal, String origin) {
        String senderFirstName = null;
        if (Objects.nonNull(portal.getUserSendMessageId())) {
            senderFirstName = userRepository.findById(portal.getUserSendMessageId()).map(User::getFirstName).orElse(null);
        }

        PortalContext context = PortalContext.builder()
            .isAdminAccess(false)
            .portalId(portal.getId())
            .platformName(portal.getPlatformName())
            .url(origin)
            .primaryLogo(portal.getPrimaryLogo())
            .secondaryLogo(portal.getSecondaryLogo())
            .primaryColor(portal.getPrimaryColor())
            .secondaryColor(portal.getSecondaryColor())
            .favicon(portal.getFavicon())
            .userSendMessageId(portal.getUserSendMessageId())
            .senderFirstName(senderFirstName)
            .welcomeMessage(portal.getWelcomeMessage())
            .isCustomDomain(portal.getIsCustomDomain())
            .build();
        PortalContextHolder.setContext(context);
    }

    // I plan to use Hibernates auto filter and activate it here.
    // But currently I have no idea at all. I will do it in the future when needed
    private void enablePortalFilter(UUID portalId) {
        Session session = entityManager.unwrap(Session.class);
        session.enableFilter("portalFilter").setParameter("portalId", portalId);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        PortalContextHolder.clearContext();
    }
}
