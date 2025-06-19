package com.home_banking_.service;

import com.home_banking_.model.Notification;

import java.util.List;

public interface NotificationService {

    Notification createNotification (Long userId, String message, String type);

    List<Notification> createNotificationByUser(Long userId);

    List<Notification> getNotificationByUser(Long userId);

    List<Notification>getUnreadByUser(Long userId);

    void markAsRead(Long notificationId);

    void deleteNotification(Long notificationId);


}
