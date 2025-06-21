package com.home_banking_.repository;

import com.home_banking_.model.Notification;
import com.home_banking_.model.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification>findByUsers_IdOrderByShippingDateDesc(Long userId);

    List<Notification> findByUsers_IdAndReadFalseOrderByShippingDateDesc(Long TypeNotification);

}
