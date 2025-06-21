package com.home_banking_.service.impl;

import com.home_banking_.exceptions.ResourceNotFoundException;
import com.home_banking_.model.Notification;
import com.home_banking_.model.Users;
import com.home_banking_.repository.NotificationRepository;
import com.home_banking_.repository.UsersRepository;
import com.home_banking_.service.NotificationService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UsersRepository usersRepository;

    public NotificationServiceImpl(NotificationRepository notificationRepository, UsersRepository usersRepository) {
        this.notificationRepository = notificationRepository;
        this.usersRepository = usersRepository;
    }


    @Override
    public Notification createNotification(Long userId, String message, String type) {
        Users users = usersRepository.findById(userId)
                .orElseThrow(()-> new ResourceNotFoundException("User not found"));

        Notification notification = new Notification();
        notification.setUsers(users);
        notification.setMessage(message);
        notification.setShippingDate(LocalDateTime.now());
        notification.setRead(false);

        return notificationRepository.save(notification);
    }


    @Override
    public List<Notification> createNotificationByUser(Long userId) {
        return notificationRepository.findByUsers_IdAndReadFalseOrderByShippingDateDesc(userId);
    }


    @Override
    public List<Notification> getNotificationByUser(Long userId) {
        return notificationRepository.findByUsers_IdOrderByShippingDateDesc(userId);
    }


    @Override
    public List<Notification> getUnreadByUser(Long userId) {
        return notificationRepository.findByUsers_IdAndReadFalseOrderByShippingDateDesc(userId);
    }


    @Override
    public void markAsRead(Long notificationId) {
        Notification noti = notificationRepository.findById(notificationId)
                .orElseThrow(()-> new ResourceNotFoundException("Notification not found"));

        noti.setRead(true);
        notificationRepository.save(noti);
    }


    @Override
    public void deleteNotification(Long notificationId) {
        if (!notificationRepository.existsById(notificationId)){
            throw new ResourceNotFoundException("Notification not found");
        }
    }
}
