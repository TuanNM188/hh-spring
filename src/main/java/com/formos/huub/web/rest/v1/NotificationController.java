package com.formos.huub.web.rest.v1;

import com.formos.huub.domain.request.notification.RequestActionNotifications;
import com.formos.huub.domain.request.notification.RequestSearchNotifications;
import com.formos.huub.framework.handler.model.ResponseData;
import com.formos.huub.framework.support.ResponseSupport;
import com.formos.huub.service.notification.NotificationsService;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.SortDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/notifications")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NotificationController {

    NotificationsService notificationsService;

    ResponseSupport responseSupport;

    @GetMapping
    @PreAuthorize("hasPermission(null, 'GET_ALL_NOTIFICATIONS_BY_USER')")
    public ResponseEntity<ResponseData> searchPortals(
        RequestSearchNotifications request,
        @SortDefault(sort = "n.createdDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        var response = notificationsService.searchNotification(request, pageable);
        return responseSupport.success(ResponseData.builder().data(response).build());
    }

    @PatchMapping("/mark-as-read")
    @PreAuthorize("hasPermission(null, 'MARK_AS_READ_NOTIFICATIONS')")
    public ResponseEntity<ResponseData> markAsReadMultiNotifications(@RequestBody RequestActionNotifications request) {
        notificationsService.markAsReads(request.getNotificationIds());
        return responseSupport.success(ResponseData.builder().build());
    }

    @PatchMapping("/mark-as-un-read")
    @PreAuthorize("hasPermission(null, 'MARK_AS_READ_NOTIFICATIONS')")
    public ResponseEntity<ResponseData> markAsUnReadMultiNotifications(@RequestBody RequestActionNotifications request) {
        notificationsService.markAsUnReads(request.getNotificationIds());
        return responseSupport.success(ResponseData.builder().build());
    }

    @GetMapping("/num-un-read")
    @PreAuthorize("hasPermission(null, 'NUM_UN_READ_NOTIFICATIONS')")
    public ResponseEntity<ResponseData> getNumNotificationUnread() {
        var response = notificationsService.countUnReadByUser();
        return responseSupport.success(ResponseData.builder().data(response).build());
    }

    @PatchMapping("/mark-all-as-read")
    @PreAuthorize("hasPermission(null, 'MARK_ALL_AS_READ_NOTIFICATIONS')")
    public ResponseEntity<ResponseData> markAsReadAllNotification() {
        notificationsService.markAsReadAll();
        return responseSupport.success(ResponseData.builder().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasPermission(null, 'DELETE_NOTIFICATION_BY_ID')")
    public ResponseEntity<ResponseData> deleteNotification(@PathVariable String id) {
        notificationsService.deleteNotifications(List.of(UUID.fromString(id)));
        return responseSupport.success(ResponseData.builder().build());
    }

    @PatchMapping("/delete")
    @PreAuthorize("hasPermission(null, 'DELETE_NOTIFICATIONS')")
    public ResponseEntity<ResponseData> deleteNotifications(@RequestBody RequestActionNotifications request) {
        notificationsService.deleteNotifications(request.getNotificationIds());
        return responseSupport.success(ResponseData.builder().build());
    }
}
