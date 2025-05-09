package com.formos.huub.config;

/**
 * Application constants.
 */
public final class Constants {

    // Regex for acceptable logins
    public static final String LOGIN_REGEX = "^(?>[a-zA-Z0-9!$&*+=?^_`{|}~.-]+@[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)*)|(?>[_.@A-Za-z0-9-]+)$";

    public static final String SYSTEM = "system";

    public static final String EMAIL_LOGIN = "email";

    public static final String USERNAME_REGEX = "^[a-zA-Z0-9][a-zA-Z0-9._]{1,49}$";

    public static final String USERNAME_SPECIAL_REGEX = "[^a-zA-Z0-9._]";

    public static final String REMOVE_HTML_REGEX = "<[^>]*>|&nbsp;";

    private Constants() {}
}
