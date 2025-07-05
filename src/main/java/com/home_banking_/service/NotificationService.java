package com.home_banking_.service;

import com.home_banking_.dto.RequestDto.NotificationRequestDto;
import com.home_banking_.dto.ResponseDto.NotificationResponseDto;

import java.util.List;

public interface NotificationService {

    NotificationResponseDto createNotification (NotificationRequestDto dto);

    List<NotificationResponseDto> createNotificationByUser(Long userId);

    List<NotificationResponseDto> getNotificationByUser(Long userId);

    List<NotificationResponseDto>getUnreadByUser(Long userId);

    void markAsRead(Long notificationId);

    void deleteNotification(Long notificationId);


}
