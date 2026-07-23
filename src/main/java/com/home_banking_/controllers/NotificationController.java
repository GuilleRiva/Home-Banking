package com.home_banking_.controllers;

import com.home_banking_.dto.response.NotificationResponseDto;
import com.home_banking_.service.NotificationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Tag(name = "Notification controller", description = "Operations related with notifications ")
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/me")
    public ResponseEntity<List<NotificationResponseDto>>
    getMyNotifications() {
        return ResponseEntity.ok(
                notificationService.getMyNotifications()
        );
    }

    @GetMapping("/me/unread")
    public ResponseEntity<List<NotificationResponseDto>>
    getMyUnreadNotifications() {
        return ResponseEntity.ok(
                notificationService.getMyUnreadNotifications()
        );
    }

    @GetMapping("/me/unread/count")
    public ResponseEntity<Long> countMyUnreadNotifications() {
        return ResponseEntity.ok(
                notificationService.countMyUnreadNotifications()
        );
    }

    @PatchMapping("/me/{notificationId}/read")
    public ResponseEntity<Void> markMyNotificationAsRead(
            @PathVariable("notificationId") Long notificationId
    ) {
        notificationService.markMyNotificationAsRead(notificationId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/me/read-all")
    public ResponseEntity<Void> markAllMyNotificationsAsRead() {
        notificationService.markAllMyNotificationsAsRead();
        return ResponseEntity.noContent().build();
    }
}
