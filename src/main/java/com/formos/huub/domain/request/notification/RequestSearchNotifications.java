package com.formos.huub.domain.request.notification;

import com.formos.huub.domain.enums.NotificationTypeEnum;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class RequestSearchNotifications {

    private NotificationTypeEnum notificationType;

    private Boolean isRead;

    private UUID userId;

    private UUID portalId;
}
