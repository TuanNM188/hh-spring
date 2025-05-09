package com.formos.huub.service.notification;

import com.formos.huub.domain.entity.Notification;
import com.formos.huub.domain.request.notification.RequestSearchNotifications;
import com.formos.huub.framework.context.PortalContextHolder;
import com.formos.huub.framework.utils.ObjectUtils;
import com.formos.huub.framework.utils.PageUtils;
import com.formos.huub.repository.NotificationRepository;
import com.formos.huub.repository.UserRepository;
import com.formos.huub.security.SecurityUtils;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NotificationsService {

    NotificationRepository notificationRepository;
    UserRepository userRepository;

    public Map<String, Object> searchNotification(RequestSearchNotifications request, Pageable pageable) {
        var currentUser = SecurityUtils.getCurrentUser(userRepository);
        if (Objects.isNull(request.getUserId())) {
            request.setUserId(currentUser.getId());
        }
        if (Objects.isNull(request.getPortalId())) {
            request.setPortalId(PortalContextHolder.getPortalId());
        }
        var data = notificationRepository.searchNotificationsByConditions(request, pageable);
        return PageUtils.toPage(data);
    }

    public void markAsReadAll() {
        var currentUser = SecurityUtils.getCurrentUser(userRepository);
        List<Notification> notifications = notificationRepository.findAllByUserId(currentUser.getId());
        notifications = notifications.stream().peek(n -> n.setIsRead(true)).toList();
        notificationRepository.saveAll(notifications);
    }

    public Integer countUnReadByUser() {
        var currentUser = SecurityUtils.getCurrentUser(userRepository);
        var portalId = PortalContextHolder.getPortalId();
        return notificationRepository.countByUserIdAndIsRead(currentUser.getId(), false, portalId);
    }

    public void markAsReads(List<UUID> notificationIds) {
        if (ObjectUtils.isEmpty(notificationIds)) {
            return;
        }
        var currentUser = SecurityUtils.getCurrentUser(userRepository);
        List<Notification> notifications = notificationRepository.findAllByUserIdAndIdIn(currentUser.getId(), notificationIds);
        notifications = notifications.stream().peek(n -> n.setIsRead(true)).toList();
        notificationRepository.saveAll(notifications);
    }

    public void markAsUnReads(List<UUID> notificationIds) {
        if (ObjectUtils.isEmpty(notificationIds)){
            return;
        }
        var currentUser = SecurityUtils.getCurrentUser(userRepository);
        List<Notification> notifications = notificationRepository.findAllByUserIdAndIdIn(currentUser.getId(),notificationIds);
        notifications = notifications
            .stream()
            .peek(n -> {
                n.setIsRead(false);
            })
            .toList();
        notificationRepository.saveAll(notifications);
    }

    public void deleteNotifications(List<UUID> notificationIds) {
        if (ObjectUtils.isEmpty(notificationIds)) {
            return;
        }
        var currentUser = SecurityUtils.getCurrentUser(userRepository);
        List<Notification> notifications = notificationRepository.findAllByUserIdAndIdIn(currentUser.getId(), notificationIds);
        notificationRepository.deleteAll(notifications);
    }
}
