package com.home_banking_.repository;

import com.home_banking_.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByRecipientEmailOrderByCreatedAtDesc(String email);

    List<Notification> findByRecipientEmailAndReadFalseOrderByCreatedAtDesc(String email);

    Optional<Notification> findByIdAndRecipientEmail(Long notificationId, String email);

    long countByRecipientEmailAndReadFalse(String email);

    List<Notification> findByRecipientEmailAndReadFalse(String email);


    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update Notification n
        set n.read = true,
            n.readAt = :readAt
        where n.recipient.email = :recipientEmail
          and n.read = false
    """)
    void markAllAsReadByRecipientEmail(@Param("recipientEmail") String email,
                                       @Param("readAt") LocalDateTime now);
}
