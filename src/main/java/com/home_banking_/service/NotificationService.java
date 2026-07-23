package com.home_banking_.service;

import com.home_banking_.dto.request.NotificationCommand;
import com.home_banking_.dto.response.NotificationResponseDto;

import java.util.List;

public interface NotificationService {

    void notifyUser(NotificationCommand command);

    List<NotificationResponseDto> getMyNotifications();

    List<NotificationResponseDto> getMyUnreadNotifications();

    long countMyUnreadNotifications();

    void markMyNotificationAsRead(Long notificationId);

    void markAllMyNotificationsAsRead();


}
