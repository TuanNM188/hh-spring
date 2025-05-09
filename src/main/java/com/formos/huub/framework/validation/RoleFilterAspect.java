package com.formos.huub.framework.validation;

import com.formos.huub.security.SecurityUtils;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Order(1)
public class RoleFilterAspect {

    @Before("@annotation(requiresRoleAdmin) || @within(requiresRoleAdmin)")
    public void checkRole(RequiresRoleAdmin requiresRoleAdmin) {
        SecurityUtils.validCurrentUserIsAdmin();
    }
}
