package com.formos.huub.security;

/**
 * Constants for Spring Security authorities.
 */
public final class AuthoritiesConstants {

    public static final String USER = "ROLE_USER";

    public static final String ANONYMOUS = "ROLE_ANONYMOUS";

    public static final String SYSTEM_ADMINISTRATOR = "ROLE_SYSTEM_ADMINISTRATOR";

    public static final String PORTAL_HOST = "ROLE_PORTAL_HOST";

    public static final String TECHNICAL_ADVISOR = "ROLE_TECHNICAL_ADVISOR";

    public static final String COMMUNITY_PARTNER = "ROLE_COMMUNITY_PARTNER";

    public static final String BUSINESS_OWNER = "ROLE_BUSINESS_OWNER";

    private AuthoritiesConstants() {}
}
