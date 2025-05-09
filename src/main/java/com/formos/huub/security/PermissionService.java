/**
 * ***************************************************
 * * Description :
 * * File        : PermissionService
 * * Author      : Hung Tran
 * * Date        : Oct 04, 2024
 * ***************************************************
 **/
package com.formos.huub.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public class PermissionService {

    public boolean hasPermission(Authentication authentication, String permissionName) {
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        for (GrantedAuthority authority : authorities) {
            if (authority.getAuthority().equals(permissionName)) {
                return true;
            }
        }
        return false;
    }
}
