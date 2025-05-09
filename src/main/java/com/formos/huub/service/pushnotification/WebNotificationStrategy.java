package com.formos.huub.service.pushnotification;

import com.formos.huub.domain.entity.Notification;
import com.formos.huub.domain.response.notification.NotificationData;
import com.formos.huub.domain.response.usersetting.IResponseValueUserSetting;
import com.formos.huub.framework.service.firebase.FirebaseNotificationService;
import com.formos.huub.framework.utils.ObjectUtils;
import com.formos.huub.repository.NotificationRepository;
import com.formos.huub.repository.UserSettingRepository;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

/**
 * ***************************************************
 * * Description :
 * * File        : WebNotificationStrategy
 * * Author      : Hung Tran
 * * Date        : Jan 13, 2025
 * ***************************************************
 **/

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@EnableAsync(proxyTargetClass = true)
public class WebNotificationStrategy implements NotificationStrategy {

    NotificationRepository notificationRepository;
    UserSettingRepository userSettingRepository;
    FirebaseNotificationService firebaseNotificationService;

    @Override
    public void processNotification(NotificationContext context) {
        List<UUID> userIdReceiverNotifications = new ArrayList<>();

        Notification baseNotification = createBaseNotification(context);

        List<Notification> notifications = userSettingRepository
            .getValueByKeyCodeAndUserId(context.getSettingKeyCode(), context.getRecipientIds())
            .stream()
            .map(setting -> buildNotificationForUser(baseNotification, setting, userIdReceiverNotifications))
            .filter(notification -> !notification.getIsRead())
            .toList();

        if (!ObjectUtils.isEmpty(userIdReceiverNotifications)) {
            notificationRepository.saveAll(notifications);
            NotificationData notificationData = buildNotificationData(baseNotification);
            firebaseNotificationService.sendNotificationToRealTimeDb(userIdReceiverNotifications, notificationData);
        }
    }

    private Notification createBaseNotification(NotificationContext context) {
        return Notification.builder()
            .title(context.getTitle())
            .content(context.getContent())
            .notificationType(context.getNotificationType())
            .referenceUrl(context.getBaseReferenceUrl())
            .portalId(context.getPortalId())
            .senderId(ObjectUtils.isEmpty(context.getSender()) ? null : context.getSender().getId())
            .isRead(false)
            .build();
    }

    private Notification buildNotificationForUser(
        Notification baseNotification,
        IResponseValueUserSetting setting,
        List<UUID> notificationReceivers
    ) {
        Notification userNotification = Notification.builder()
            .notificationType(baseNotification.getNotificationType())
            .title(baseNotification.getTitle())
            .content(baseNotification.getContent())
            .referenceUrl(baseNotification.getReferenceUrl())
            .isRead(true)
            .userId(setting.getUserId())
            .portalId(baseNotification.getPortalId())
            .senderId(baseNotification.getSenderId())
            .build();

        if (Boolean.TRUE.equals(setting.getIsEnableWeb())) {
            notificationReceivers.add(setting.getUserId());
            userNotification.setIsRead(false);
        }

        return userNotification;
    }

    private NotificationData buildNotificationData(Notification notification) {
        NotificationData notificationData = new NotificationData();
        BeanUtils.copyProperties(notification, notificationData);
        notificationData.setTimestamp(String.valueOf(Timestamp.from(Instant.now()).getTime()));
        notificationData.setPortalId(Optional.ofNullable(notification.getPortalId()).map(UUID::toString).orElse(null));
        return notificationData;
    }
}
