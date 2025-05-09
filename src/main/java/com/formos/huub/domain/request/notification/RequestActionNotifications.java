package com.formos.huub.domain.request.notification;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class RequestActionNotifications {

    List<UUID> notificationIds;
}
