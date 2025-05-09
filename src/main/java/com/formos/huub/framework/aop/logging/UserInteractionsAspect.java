package com.formos.huub.framework.aop.logging;

import com.formos.huub.domain.entity.User;
import com.formos.huub.domain.entity.UserInteractions;
import com.formos.huub.domain.enums.ActionTypeEnum;
import com.formos.huub.domain.enums.ScreenTypeEnum;
import com.formos.huub.domain.enums.StatusEnum;
import com.formos.huub.domain.response.funding.ResponseDetailFunding;
import com.formos.huub.domain.response.funding.ResponseStatusFunding;
import com.formos.huub.framework.context.PortalContextHolder;
import com.formos.huub.repository.UserInteractionsRepository;
import com.formos.huub.repository.UserRepository;
import com.formos.huub.security.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.UUID;


@Aspect
@Component
@Slf4j
public class UserInteractionsAspect {
    @Autowired
    private UserInteractionsRepository userInteractionsRepository;

    @Autowired
    private UserRepository userRepository;

    @Pointcut("execution(* com.formos.huub.service.fundingservice.FundingService.getDetailFundingById(..))")
    public void viewDetailsFunding() {
    }

    @Pointcut("execution(* com.formos.huub.service.fundingservice.FundingService.favoriteFunding(..))")
    public void favoriteFunding() {
    }

    @Pointcut("execution(* com.formos.huub.service.fundingservice.FundingService.userSubmitFunding(..))")
    public void submittedFunding() {
    }

    /**
     * Handle Sync Events Failure
     *
     * @param joinPoint JoinPoint
     */
    @AfterReturning(pointcut = "viewDetailsFunding()", returning = "result")
    public void handleGetDetailFunding(JoinPoint joinPoint, Object result) {
        try {
            ResponseDetailFunding responseDetailFunding = (ResponseDetailFunding) result;
            saveUserInteractionsRepository(ScreenTypeEnum.FUNDING_OPPORTUNITIES, ActionTypeEnum.PAGE_VIEW, responseDetailFunding.getId());
        } catch (Exception e) {
            log.error("Error while save user interactions", e);
            e.getStackTrace();
        }
    }

    /**
     * Handle Sync Events Failure
     *
     * @param joinPoint JoinPoint
     */
    @AfterReturning(pointcut = "submittedFunding()", returning = "result")
    public void handleFavoriteFunding(JoinPoint joinPoint, Object result) {
        try {
            ResponseStatusFunding response = (ResponseStatusFunding) result;
            saveUserInteractionsRepository(ScreenTypeEnum.FUNDING_OPPORTUNITIES, StatusEnum.ACTIVE.equals(response.getStatus())
                ? ActionTypeEnum.FAVORITE : ActionTypeEnum.UN_FAVORITE, response.getId());
        } catch (Exception e) {
            log.error("Error while save user interactions", e);
            e.getStackTrace();
        }
    }

    /**
     * Handle Sync Events Failure
     *
     * @param joinPoint JoinPoint
     */
    @AfterReturning(pointcut = "favoriteFunding()", returning = "result")
    public void handleSubmittedFunding(JoinPoint joinPoint, Object result) {
        try {
            ResponseStatusFunding response = (ResponseStatusFunding) result;
            saveUserInteractionsRepository(ScreenTypeEnum.FUNDING_OPPORTUNITIES, StatusEnum.ACTIVE.equals(response.getStatus())
                ? ActionTypeEnum.MARK_SUBMITTED : ActionTypeEnum.UN_MARK_SUBMITTED, response.getId());
        } catch (Exception e) {
            log.error("Error while save user interactions", e);
            e.getStackTrace();
        }
    }

    private void saveUserInteractionsRepository(ScreenTypeEnum screenType, ActionTypeEnum actionType, UUID fundingId) {
        var currentUser = getCurrentUser();
        if (Objects.isNull(currentUser)) {
            return;
        }
        var portalId = PortalContextHolder.getPortalId();
        var userInteractions = UserInteractions.builder()
            .userId(currentUser.getId())
            .portalId(portalId)
            .screenType(screenType)
            .actionType(actionType)
            .entryId(fundingId)
            .build();
        userInteractionsRepository.save(userInteractions);
    }

    private User getCurrentUser() {
        return SecurityUtils.getCurrentUserLogin().flatMap(userRepository::findOneWithAuthoritiesByLogin)
            .orElse(null);
    }
}
