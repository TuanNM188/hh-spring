package com.formos.huub.repository;

import com.formos.huub.domain.entity.Notification;
import com.formos.huub.domain.request.notification.RequestSearchNotifications;
import com.formos.huub.domain.response.notification.IResponseNotifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {


    @Query("select n.id as id, n.title as title, n.createdDate as createdDate, n.isRead as isRead, n.referenceUrl as referenceUrl," +
        "  coalesce(u.imageUrl, nu.imageUrl )as imageUrl" +
        " from Notification n left join User u on (u.id = n.senderId or (u.login = n.createdBy and n.senderId is null)) " +
        " join User nu on nu.id = n.userId " +
        " where n.userId = :#{#cond.userId} " +
        " and (:#{#cond.notificationType} is null OR n.notificationType = :#{#cond.notificationType})" +
        " and (:#{#cond.portalId} is null OR n.portalId is null OR n.portalId = :#{#cond.portalId})" +
        " and (:#{#cond.isRead} is null OR n.isRead = :#{#cond.isRead})")
    Page<IResponseNotifications> searchNotificationsByConditions(@Param("cond")RequestSearchNotifications cond, Pageable pageable);

    List<Notification> findAllByUserId(UUID userId);

    List<Notification> findAllByUserIdAndIdIn(UUID userId, List<UUID> ids);

    @Query("select count(1) from Notification n where n.userId = :userId and n.isRead = :isRead " +
        " and (:portalId is null or n.portalId = :portalId)")
    Integer countByUserIdAndIsRead(UUID userId, Boolean isRead, UUID portalId);
}
