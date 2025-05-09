package com.formos.huub.domain.response.notification;

import com.formos.huub.domain.enums.NotificationTypeEnum;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class NotificationData {

    private UUID id;

    private UUID userId;

    private String content;

    private NotificationTypeEnum notificationType;

    private String referenceUrl;

    private String title;

    private String timestamp;

    private String portalId;
}
