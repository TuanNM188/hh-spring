package com.formos.huub.service.pushnotification;

import com.formos.huub.domain.entity.User;
import com.formos.huub.domain.enums.NotificationTypeEnum;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * ***************************************************
 * * Description :
 * * File        : NotificationContext
 * * Author      : Hung Tran
 * * Date        : Jan 13, 2025
 * ***************************************************
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NotificationContext {

    String title;
    String emailTitle;
    String content;
    String baseReferenceUrl;
    String referenceUrl;
    UUID portalId;
    NotificationTypeEnum notificationType;
    List<UUID> recipientIds;
    List<String> recipientEmail;
    User sender;
    Map<String, String> portalInfo;
    String settingKeyCode;
    String templatePath;
    String description;
    String footerLink;
    Map<String, Object> additionalData;
}
