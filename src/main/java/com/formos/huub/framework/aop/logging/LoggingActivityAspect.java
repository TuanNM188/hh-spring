package com.formos.huub.framework.aop.logging;

import com.formos.huub.domain.entity.ActivityLog;
import com.formos.huub.domain.enums.ActivityLogTypeEnum;
import com.formos.huub.domain.request.authenticate.RequestTokenRefresh;
import com.formos.huub.domain.response.authenticate.ResponseLogin;
import com.formos.huub.domain.response.authenticate.ResponseLoginAsUser;
import com.formos.huub.domain.response.authenticate.ResponseTokenRefresh;
import com.formos.huub.framework.enums.HeaderEnum;
import com.formos.huub.framework.utils.StringUtils;
import com.formos.huub.repository.ActivityLogRepository;
import com.formos.huub.repository.TokenManagerRepository;
import com.formos.huub.security.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Slf4j
public class LoggingActivityAspect {
    // https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/

    @Autowired
    private JwtDecoder jwtDecoder;

    @Autowired
    private ActivityLogRepository activityLogRepository;

    @Autowired
    private TokenManagerRepository tokenManagerRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    private final Object lock = new Object();

    private static final long LOG_INTERVAL = 5000; // 5 sec

    // Pointcut for signIn method
    @Pointcut("execution(* com.formos.huub.service.tokenmanager.TokenManagerService.signIn(..))")
    public void signInPointcut() {
    }

    @Pointcut("execution(* com.formos.huub.service.tokenmanager.TokenManagerService.signInAsUser(..))")
    public void signInAsUserPointcut() {
    }

    // Pointcut for signOut method
    @Pointcut("execution(* com.formos.huub.service.tokenmanager.TokenManagerService.signOut(..))")
    public void signOutPointcut() {
    }

    @Pointcut("execution(* com.formos.huub.service.tokenmanager.TokenManagerService.refreshToken(..))")
    public void refreshTokenPointcut() {
    }

    /**
     * Log the sign in activity.
     *
     * @param joinPoint JoinPoint
     * @param result    Object
     */
    @AfterReturning(pointcut = "signInPointcut()", returning = "result")
    public void logAfterLogin(JoinPoint joinPoint, Object result) {
        try {
            // Get user information from result object
            ResponseLogin responseLogin = (ResponseLogin) result;
            String login = responseLogin.getUsername();

            // Get device information from request
            ActivityLog activityLog = createActivityLog(login, ActivityLogTypeEnum.LOGIN);
            activityLog.setAccessToken(responseLogin.getAccessToken());

            // Save the ActivityLog object to the database
            saveLogActivity(activityLog);
        } catch (Exception e) {
            log.error("Error while logging login activity::", e);
            e.getStackTrace();
        }
    }

    /**
     * Log the sign in as user activity.
     *
     * @param joinPoint JoinPoint
     * @param result    Object
     */
    @AfterReturning(pointcut = "signInAsUserPointcut()", returning = "result")
    public void logAfterLoginAsUser(JoinPoint joinPoint, Object result) {
        try {

            // Get user information from result object
            ResponseLoginAsUser responseLoginAsUser = (ResponseLoginAsUser) result;
            String login = responseLoginAsUser.getUsername();

            // Get device information from request
            ActivityLog activityLog = createActivityLog(login, ActivityLogTypeEnum.LOGIN_AS_USER);
            activityLog.setAccessToken(responseLoginAsUser.getAccessToken());
            activityLog.setNote(String.format("FROM ADMIN: %s >>> LOGIN AS :: %s", responseLoginAsUser.getAdminUser(), login));
            // Save the ActivityLog object to the database
            saveLogActivity(activityLog);
        } catch (Exception e) {
            log.error("Error while logging login activity::", e);
            e.getStackTrace();
        }
    }

    /**
     * Log the sign out activity.
     *
     * @param joinPoint JoinPoint
     * @param result    Object
     */
    @AfterReturning(pointcut = "signOutPointcut()", returning = "result")
    public void logAfterLogout(JoinPoint joinPoint, Object result) {
        try {
            // Get user information from result object
            String login = SecurityUtils.getCurrentUserLogin().orElse("");

            // Get device information from request
            ActivityLog activityLog = createActivityLog(login, ActivityLogTypeEnum.LOGOUT);

            // Save the ActivityLog object to the database
            saveLogActivity(activityLog);
        } catch (Exception e) {
            log.error("Error while logging logout activity::", e);
            e.getStackTrace();
        }
    }

    /**
     * Log the refresh token activity.
     *
     * @param joinPoint ProceedingJoinPoint
     * @return Object
     * @throws Throwable Exception
     */
    @Around("refreshTokenPointcut()")
    public Object logAroundRefreshToken(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        RequestTokenRefresh requestTokenRefresh = (RequestTokenRefresh) args[0];
        var tokenOptional = tokenManagerRepository.findByRefreshToken(requestTokenRefresh.getRefreshToken());

        String login = null;
        if (tokenOptional.isPresent()) {
            login = tokenOptional.get().getLogin();
        }

        ActivityLog activityLog = createActivityLog(login, ActivityLogTypeEnum.REFRESH_TOKEN);

        var activityLogOptional = activityLogRepository.findActivityLogByAccessTokenAndRefreshToken(
            activityLog.getAccessToken(), requestTokenRefresh.getRefreshToken());

        if (activityLogOptional.isPresent()) {
            var activityLogEntity = activityLogOptional.get();
            long currentTime = System.currentTimeMillis();
            long lastLoggedTime = activityLogEntity.getCreatedDate().toEpochMilli();
            if (currentTime - lastLoggedTime < LOG_INTERVAL) {
                return joinPoint.proceed();
            }
        }

        try {
            // Proceed with the original method
            ResponseTokenRefresh result = (ResponseTokenRefresh) joinPoint.proceed();
            activityLog.setAccessToken(result.getAccessToken());
            activityLog.setRefreshToken(requestTokenRefresh.getRefreshToken());
            return result;
        } catch (Exception e) {
            log.error("Error while logging refresh token activity::", e);
            activityLog.setActivityType(ActivityLogTypeEnum.REFRESH_TOKEN_EXPIRE.getValue());
            throw e;
        } finally {
            // Save the ActivityLog object to the database
            if (StringUtils.isNotBlank(activityLog.getLogin())) {
                saveLogActivity(activityLog);
            }
        }
    }

    /**
     * Log the token validation result.
     *
     * @param joinPoint ProceedingJoinPoint
     * @param authToken String
     * @return boolean
     */
    @Around("execution(* com.formos.huub.security.jwt.TokenProvider.validateToken(..)) && args(authToken)")
    public Object logAroundValidateToken(ProceedingJoinPoint joinPoint, String authToken) {
        boolean isValid = true;
        String login = null;
        ActivityLog activityLog = createActivityLog(login, ActivityLogTypeEnum.ACCESS_TOKEN_EXPIRE);

        try {
            isValid = (boolean) joinPoint.proceed();
            Jwt jwt = jwtDecoder.decode(authToken);
            login = jwt.getSubject();
            activityLog.setLogin(login);
            return isValid;
        } catch (Throwable e) {
            log.error("Error during token validation: {}", e.getMessage());
            activityLog.setNote(e.getMessage());
            activityLog.setLogin(login);
            return false;
        } finally {
            if (!isValid && StringUtils.isNotBlank(activityLog.getLogin())) {
                synchronized (lock) {
                    // Check if there is a log for this token
                    boolean isLogged = activityLogRepository.existsByLoginAndActivityTypeAndAccessToken(
                        login, ActivityLogTypeEnum.ACCESS_TOKEN_EXPIRE.getValue(), authToken);
                    if (!isLogged) {
                        saveLogActivity(activityLog);
                    }
                }
            }
        }
    }

    /**
     * Save the given activity log using a new transaction.
     *
     * @param activityLog LogPaymentEntry
     */
    private void saveLogActivity(final ActivityLog activityLog) {
        this.transactionTemplate.setPropagationBehavior(TransactionTemplate.PROPAGATION_REQUIRES_NEW);
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                try {
                    activityLogRepository.save(activityLog);
                } catch (Exception e) {
                    // Log the exception to logging framework
                    log.error("Could not save activity log, transaction will be rolled back: {}", e.getMessage(), e);
                    e.getStackTrace();
                    // Mark the transaction as rollback-only
                    status.setRollbackOnly();
                }
            }
        });
    }

    /**
     * Create an ActivityLog object from the request headers.
     *
     * @param login String
     * @return ActivityLog
     */
    public ActivityLog createActivityLog(String login, ActivityLogTypeEnum activityType) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String deviceName = request.getHeader(HeaderEnum.X_DEVICE_NAME.getValue());
        String deviceType = request.getHeader(HeaderEnum.X_DEVICE_TYPE.getValue());
        String deviceInfo = request.getHeader(HeaderEnum.X_DEVICE_INFO.getValue());
        String operatingSystem = request.getHeader(HeaderEnum.X_OPERATING_SYSTEM.getValue());
        String userAgent = request.getHeader(HeaderEnum.X_USER_AGENT.getValue());
        String browser = request.getHeader(HeaderEnum.X_BROWSER.getValue());
        String browserVersion = request.getHeader(HeaderEnum.X_BROWSER_VERSION.getValue());
        String additionalInfo = request.getHeader(HeaderEnum.X_ADDITIONAL_INFO.getValue());
        String ipAddress = request.getRemoteAddr();
        String authHeader = request.getHeader(HeaderEnum.AUTHORIZATION.getValue());

        ActivityLog activityLog = new ActivityLog();
        activityLog.setLogin(login);
        activityLog.setDeviceName(deviceName);
        activityLog.setDeviceType(deviceType);
        activityLog.setDeviceInfo(deviceInfo);
        activityLog.setOperatingSystem(operatingSystem);
        activityLog.setBrowser(browser);
        activityLog.setBrowserVersion(browserVersion);
        activityLog.setIpAddress(ipAddress);
        activityLog.setUserAgent(userAgent);
        activityLog.setAdditionalInfo(additionalInfo);
        activityLog.setActivityType(activityType.getValue());

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String accessToken = authHeader.substring(7);
            activityLog.setAccessToken(accessToken);
        }
        return activityLog;
    }
}
