package com.home_banking_.controllers;

import com.home_banking_.model.Notification;
import com.home_banking_.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.aspectj.weaver.ast.Not;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping
    public ResponseEntity<Notification> createNotification(@RequestParam Long userId,
                                                           @RequestParam String message,
                                                           @RequestParam String type){


        Notification created = notificationService.createNotification(userId, message, type);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }


    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Notification>> getNotificationByUser(@PathVariable Long userId){
        List<Notification> notifications = notificationService.getNotificationByUser(userId);

        return ResponseEntity.ok(notifications);
    }


    @GetMapping("/user/{userId}/unread")
    public ResponseEntity<List<Notification>> getUnReadByUser(@PathVariable Long userId){
        List<Notification> unread = notificationService.getUnreadByUser(userId);

        return ResponseEntity.ok(unread);
    }


    @PostMapping("/{notificationId}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long notificationId){
        notificationService.markAsRead(notificationId);

        return ResponseEntity.noContent().build();
    }


    @PostMapping("/simulate/{userId}")
    public ResponseEntity<List<Notification>>simulateNotifications(@PathVariable Long userId){
        List<Notification> list = notificationService.createNotificationByUser(userId);

        return ResponseEntity.status(HttpStatus.CREATED).body(list);
    }


    @DeleteMapping
    public ResponseEntity<Void>deleteNotification(@PathVariable Long notificationId){
        notificationService.deleteNotification(notificationId);

        return ResponseEntity.noContent().build();
    }
}
