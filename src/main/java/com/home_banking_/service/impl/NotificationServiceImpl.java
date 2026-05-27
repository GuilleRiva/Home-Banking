package com.home_banking_.service.impl;

import com.home_banking_.dto.request.NotificationRequestDto;
import com.home_banking_.dto.response.NotificationResponseDto;
import com.home_banking_.enums.TypeNotification;
import com.home_banking_.exceptions.custom.ResourceNotFoundException;
import com.home_banking_.mappers.NotificationMapper;
import com.home_banking_.model.Notification;
import com.home_banking_.model.Users;
import com.home_banking_.repository.NotificationRepository;
import com.home_banking_.repository.UsersRepository;
import com.home_banking_.service.NotificationService;
import com.home_banking_.service.security.CurrentUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UsersRepository usersRepository;
    private final NotificationMapper notificationMapper;
    private final CurrentUserService currentUserService;

    public NotificationServiceImpl(NotificationRepository notificationRepository, UsersRepository usersRepository, NotificationMapper notificationMapper, CurrentUserService currentUserService) {
        this.notificationRepository = notificationRepository;
        this.usersRepository = usersRepository;
        this.notificationMapper = notificationMapper;
        this.currentUserService = currentUserService;
    }


    @Override
    public NotificationResponseDto createNotification(NotificationRequestDto dto) {

        Users users = usersRepository.findById(Long.valueOf(dto.getUserId()))
                .orElseThrow(()-> new ResourceNotFoundException(
                        "User not found"
                ));

        Notification notification = notificationMapper.toEntity(dto);
        notification.setUsers(users);
        notification.setMessage(dto.getMessage());
        notification.setShippingDate(LocalDateTime.now());
        notification.setRead(false);

        notificationRepository.save(notification);

        log.info("Notification created usedId: {}", dto.getUserId());
        return notificationMapper.toDto(notification);
    }



    @Override
    public List<NotificationResponseDto> createNotificationByUser(Long userId) {

        Users users = usersRepository.findById(userId)
                .orElseThrow(()->  new ResourceNotFoundException(
                        "User not found"
                ));

        List<Notification> notifications = new ArrayList<>();

        Notification n1 = new Notification();
        n1.setUsers(users);
        n1.setMessage("Your loan was approved");
        n1.setShippingDate(LocalDateTime.now());
        n1.setRead(false);
        n1.setTypeNotification(TypeNotification.TRANSACCION_REALIZADA);

        Notification n2 = new Notification();
        n2.setUsers(users);
        n2.setMessage("new card available");
        n2.setShippingDate(LocalDateTime.now());
        n2.setRead(false);
        n2.setTypeNotification(TypeNotification.TRANSACCION_REALIZADA);

        notifications.add(n1);
        notifications.add(n2);

        notificationRepository.saveAll(notifications);

        log.info("Has been created {} automatic notifications for user ID: {}", notifications.size(), userId);
        return notifications.stream()
                .map(notificationMapper::toDto)
                .collect(Collectors.toList());
    }


    @Override
    public List<NotificationResponseDto> getNotificationByUser(Long userId) {

        Users users = usersRepository.findById(userId)
                .orElseThrow(()->  new ResourceNotFoundException(
                        "User not found"
                ));

        List<Notification> notifications = notificationRepository.findByUsers_IdOrderByShippingDateDesc(users.getId());

        log.info("Total notifications found for user ID {}: {}", userId, notifications.size());
        return notifications.stream()
                .map(notificationMapper::toDto)
                .collect(Collectors.toList());
    }



    @Override
    public List<NotificationResponseDto> getUnreadByUser(Long userId) {

        Users users = usersRepository.findById(userId)
                .orElseThrow(()-> new ResourceNotFoundException(
                        "User not found"
                ));

        List<Notification> unread = notificationRepository.findByUsers_IdAndReadFalseOrderByShippingDateDesc(users.getId());

        log.info("Total unread notifications for user ID {}: {}", userId, unread.size());
        return unread.stream()
                .map(notificationMapper::toDto)
                .collect(Collectors.toList());
    }


    @Override
    public void markAsRead(Long notificationId) {

        Notification noti = notificationRepository.findById(notificationId)
                .orElseThrow(()-> new ResourceNotFoundException(
                        "Notification not found"
                ));

        noti.setRead(true);
        notificationRepository.save(noti);

        log.info("Notification marked as read successfully. ID: {}", notificationId);
    }




    @Override
    public void deleteNotification(Long notificationId) {

        if (!notificationRepository.existsById(notificationId)){
            log.warn("Notification not found when trying to delete. ID: {}", notificationId);
            throw new ResourceNotFoundException("Notification not found");
        }
        notificationRepository.deleteById(notificationId);

        log.info("Notification successfully removed. ID: {}", notificationId);
    }
}
