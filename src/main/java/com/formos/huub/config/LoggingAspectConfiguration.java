package com.formos.huub.config;

import com.formos.huub.aop.logging.LoggingAspect;
import com.formos.huub.framework.aop.logging.LoggingActivityAspect;
import com.formos.huub.framework.aop.logging.UserInteractionsAspect;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;
import tech.jhipster.config.JHipsterConstants;

@Configuration
@EnableAspectJAutoProxy
public class LoggingAspectConfiguration {

    @Bean
    @Profile(JHipsterConstants.SPRING_PROFILE_DEVELOPMENT)
    public LoggingAspect loggingAspect(Environment env) {
        return new LoggingAspect(env);
    }

    @Bean
    public LoggingActivityAspect loggingActivityAspect() {
        return new LoggingActivityAspect();
    }

    @Bean
    public UserInteractionsAspect userInteractionsAspect() {
        return new UserInteractionsAspect();
    }
}
