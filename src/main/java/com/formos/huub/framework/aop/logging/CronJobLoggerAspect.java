package com.formos.huub.framework.aop.logging;

import com.formos.huub.domain.entity.LogCronJobEntries;
import com.formos.huub.domain.enums.SyncEventStatusEnum;
import com.formos.huub.repository.LogCronJobEntriesRepository;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Set;

@Aspect
@Component
public class CronJobLoggerAspect {

    @Autowired
    private LogCronJobEntriesRepository logRepository;

    // List of class names to ignore
    private static final Set<String> IGNORED_CLASSES = Set.of(
        "WebhookEventService"
    );

    @Around("@annotation(org.springframework.scheduling.annotation.Scheduled)")
    public Object logCronExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        String jobName = joinPoint.getSignature().getName();
        var className = joinPoint.getTarget().getClass().getSimpleName();
        if (IGNORED_CLASSES.contains(className)) {
            return joinPoint.proceed();
        }
        LogCronJobEntries log = new LogCronJobEntries();
        log.setMethodName(jobName);
        log.setServiceName(className);
        log.setTimestamp(Instant.now());

        try {
            Object result = joinPoint.proceed();
            log.setLogStatus(SyncEventStatusEnum.SUCCESS);
            return result;
        } catch (Exception e) {
            log.setLogStatus(SyncEventStatusEnum.ERROR);
            log.setProblem(String.format("%s [ %s ]", e.toString(), e.getCause().toString()));
            throw e;
        } finally {
            logRepository.save(log);
        }
    }
}

