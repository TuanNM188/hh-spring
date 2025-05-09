package com.formos.huub.domain.response.notification;

import com.formos.huub.domain.enums.NotificationTypeEnum;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
public class ResponseListNotification {

    private UUID id;

    private String title;

    private String fullName;

    private String imageUrl;

    private Instant createDate;

    private NotificationTypeEnum notificationType;

    private String referenceUrl;

    private Boolean isRead;
}
