package com.formos.huub.framework.constant;

/**
 * ***************************************************
 * * Description :
 * * File        : AppConstants
 * * Author      : Hung Tran
 * * Date        : Jun 09, 2024
 * ***************************************************
 **/
public final class AppConstants {

    public static final String KEY_SPACE = " ";
    public static final String KEY_SPACE_REGEX = "\\s+";
    public static final String KEY_EMPTY = "";
    public static final String KEY_DOLA = "$";
    public static final String KEY_HASHTAG = "#";
    public static final String KEY_SLASH = "/";
    public static final String KEY_COMMA = ",";
    public static final String KEY_QUESTION = "?";
    public static final String KEY_AND = "&";
    public static final String WIN = "win";
    public static final String MAC = "mac";

    public static final long SECONDS_OF_A_HOUR = 60 * 60;
    public static final long SECONDS_OF_A_MINUTE = 60;

    public static final String DEFAULT_LANGUAGE = "en";
    public static final String DEFAULT_TRANSLATE_SOURCE_LANG = "auto";

    // Regex
    public static final String SSN_REGEX = "^(?!666|000|9\\d{2})\\d{3}-(?!00)\\d{2}-(?!0{4})\\d{4}$";
    public static final String UUID_V4_REGEX = "[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-4[a-fA-F0-9]{3}-[89abAB][a-fA-F0-9]{3}-[a-fA-F0-9]{12}";
    public static final String EMAIL_REGEX = ".+[@].+[\\.].+";
    public static final String LOGIN_REGEX = "^(?>[a-zA-Z0-9!$&*+=?^_`{|}~.-]+@[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)*)|(?>[_.@A-Za-z0-9-]+)$";
    public static final String PASSWORD_REGEX = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*()-+=])(?=\\S+$).{8,}$";
    public static final String SPOTIFY_URL_REGEX = "^https:\\/\\/(open\\.)?spotify\\.com\\/.*$";
    public static final String YOUTUBE_OR_VIMEO_URL_REGEX = "^https?:\\/\\/(www\\.)?(youtube\\.com|youtu\\.be|vimeo\\.com)\\/.*$";
    public static final String SUB_DOMAIN_REGEX = "^(?!-)[a-zA-Z0-9-]{3,30}(?<!-)$";
    public static final String PORTAL_URL_REGEX = "^(https?|ftp)://[^\\s/$.?#].[^\\s]*$";
    public static final Integer LIMIT_IMAGE_IN_POST = 8;
    public static final Integer LIMIT_VIDEO_IN_POST = 1;
    public static final Integer MAX_SIZE_VIDEO_IN_POST = 128;
    public static final Integer MAX_SIZE_FILE_IN_POST = 16;
    public static final Integer RETRIES_LIMIT = 3;
    public static final String EVENT_LIVE = "live";
    public static final String EVENT_STARTED = "started";

    public static final String PORTAL_NAME = "portalName";
    public static final String PORTAL_URL = "portalUrl";
    public static final String PREFERENCE_URL = "referenceUrl";
    public static final String RECEIVER_NAME = "receiverName";
    public static final String SENDER_NAME = "senderName";
    public static final String COMMUNITY_BOARD = "Community Board";
    public static final String LOGO_IMAGE = "logoImage";
    public static final String BASE_URL = "baseUrl";
    public static final String PORTAL_LOGO = "portalLogo";

    public static final String COMPANY_WEBSITE = "myhuub.com";
    public static final String COMPANY_TAGLINE = "Small Business Support Made Simple";
    public static final String DEFAULT_AVATAR = "Logo/default-avatar.png";
    public static final String DEFAULT_PORTAL_LOGO = "Logo/HUUB-Logo.png";
}
