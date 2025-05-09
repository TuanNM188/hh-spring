/**
 * ***************************************************
 * * Description :
 * * File        : CustomPermissionEvaluator
 * * Author      : Hung Tran
 * * Date        : Oct 10, 2024
 * ***************************************************
 **/
package com.formos.huub.security;

import java.io.Serializable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class CustomPermissionEvaluator implements PermissionEvaluator {

    @Autowired
    private PermissionService permissionService;

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        if (permission instanceof String) {
            String permissionName = (String) permission;
            return permissionService.hasPermission(authentication, permissionName);
        }
        return false;
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        return false;
    }
}
