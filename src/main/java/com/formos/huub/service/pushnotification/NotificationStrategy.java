package com.formos.huub.service.pushnotification;

/**
 * ***************************************************
 * * Description :
 * * File        : NotificationStrategy
 * * Author      : Hung Tran
 * * Date        : Jan 13, 2025
 * ***************************************************
 **/
public interface NotificationStrategy {
    void processNotification(NotificationContext notificationContext);
}
