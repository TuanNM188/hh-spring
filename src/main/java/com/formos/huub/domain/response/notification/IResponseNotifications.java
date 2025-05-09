package com.formos.huub.domain.response.notification;

import com.formos.huub.domain.enums.NotificationTypeEnum;

import java.time.Instant;
import java.util.UUID;

public interface IResponseNotifications {

     UUID getId();

     String getTitle();

     String getFullName();

     String getImageUrl();

     Instant getCreatedDate();

     NotificationTypeEnum getNotificationType();

     String getReferenceUrl();

     Boolean getIsRead();
}
