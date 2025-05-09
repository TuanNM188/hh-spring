package com.formos.huub.framework.aop.logging;

import com.formos.huub.domain.entity.CalendarIntegration;
import com.formos.huub.domain.entity.LogSyncEvent;
import com.formos.huub.domain.enums.SyncEventStatusEnum;
import com.formos.huub.repository.CalendarIntegrationRepository;
import com.formos.huub.repository.LogSyncEventRepository;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;

@Aspect
@Component
@Slf4j
public class SyncEventFailureAspect {

    @Autowired
    private LogSyncEventRepository logSyncEventRepository;

    @Autowired
    private CalendarIntegrationRepository calendarIntegrationRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Value("${schedule.sync.retry-times}")
    Long retryTimes;

    @Pointcut("execution(* com.formos.huub.service.calendarevent.CalendarEventService.handleSaveEvent(..)) || " +
        "execution(* com.formos.huub.service.externalcalendar.ICalService.getEventsFromUrl())")
    public void syncEventPointcut() {
    }

    /**
     * Update CalendarIntegration Status After Sync
     *
     * @param joinPoint JoinPoint
     */
    @AfterReturning(pointcut = "syncEventPointcut()")
    public void updateCalendarIntegrationStatusAfterSync(JoinPoint joinPoint) {
        try {
            Object[] args = joinPoint.getArgs();
            CalendarIntegration calendarIntegration = (CalendarIntegration) args[0];
            updateCalendarIntegrationStatus(calendarIntegration, SyncEventStatusEnum.SUCCESS, 0);
        } catch (Exception e) {
            log.error("Error while updating Calendar Integration sync status::", e);
            e.getStackTrace();
        }
    }

    /**
     * Handle Sync Events Failure
     *
     * @param joinPoint JoinPoint
     * @param e Exception
     */
    @AfterThrowing(pointcut = "syncEventPointcut()", throwing = "e")
    public void handleSyncEventsFailure(JoinPoint joinPoint, Exception e) {
        this.transactionTemplate.setPropagationBehavior(TransactionTemplate.PROPAGATION_REQUIRES_NEW);
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                try {
                    Object[] args = joinPoint.getArgs();
                    CalendarIntegration calendarIntegration = (CalendarIntegration) args[0];

                    int retryCount = calendarIntegration.getRetryCount() + 1;
                    SyncEventStatusEnum syncEventStatus = retryCount < retryTimes ? SyncEventStatusEnum.RETRYING : SyncEventStatusEnum.ERROR;

                    // Update the sync status
                    updateCalendarIntegrationStatus(calendarIntegration, syncEventStatus, retryCount);

                    // Log the sync event failure
                    logSyncEventFailure(calendarIntegration, syncEventStatus, e);
                } catch (Exception e) {
                    log.error("Error while logging sync events::", e);
                    e.getStackTrace();
                }
            }
        });
    }

    /**
     * Update CalendarIntegration Status
     *
     * @param calendarIntegration CalendarIntegration
     * @param status SyncEventStatusEnum
     * @param retryCount int
     */
    private void updateCalendarIntegrationStatus(CalendarIntegration calendarIntegration, SyncEventStatusEnum status, int retryCount) {
        calendarIntegration.setSyncEventStatus(status);
        calendarIntegration.setRetryCount(retryCount);
        calendarIntegration.setLastSync(Instant.now());
        calendarIntegrationRepository.save(calendarIntegration);
    }

    /**
     * Log Sync Event Failure
     *
     * @param calendarIntegration CalendarIntegration
     * @param status SyncEventStatusEnum
     * @param e Exception
     */
    private void logSyncEventFailure(CalendarIntegration calendarIntegration, SyncEventStatusEnum status, Exception e) {
        LogSyncEvent logSyncEvent = LogSyncEvent.builder()
            .calendarIntegrationId(calendarIntegration.getId())
            .url(calendarIntegration.getUrl())
            .problem(String.join("|", status.getValue(), e.getLocalizedMessage()))
            .timestamp(Instant.now())
            .portalId(calendarIntegration.getPortal().getId())
            .integrateBy(calendarIntegration.getIntegrateBy())
            .build();
        logSyncEventRepository.save(logSyncEvent);
    }
}
