package com.formos.huub.framework.context;

import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

/**
 * ***************************************************
 * * Description :
 * * File        : PortalContext
 * * Author      : Hung Tran
 * * Date        : Sep 28, 2024
 * ***************************************************
 **/
@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PortalContext {

    UUID portalId;
    String platformName;
    boolean isAdminAccess;
    String url;
    String primaryLogo;
    String secondaryLogo;
    String primaryColor;
    String secondaryColor;
    String favicon;
    UUID userSendMessageId;
    String senderFirstName;
    String welcomeMessage;
    boolean isCustomDomain;
}
