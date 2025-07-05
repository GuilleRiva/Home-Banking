package com.home_banking_.controllers;

import com.home_banking_.dto.RequestDto.NotificationRequestDto;
import com.home_banking_.dto.ResponseDto.NotificationResponseDto;
import com.home_banking_.model.Notification;
import com.home_banking_.service.NotificationService;
import jakarta.validation.Valid;
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
    public ResponseEntity<NotificationResponseDto> createNotification(@RequestParam @Valid NotificationRequestDto dto){


        NotificationResponseDto created = notificationService.createNotification(dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }


    @GetMapping("/user/{userId}")
    public ResponseEntity<List<NotificationResponseDto>> getNotificationByUser(@PathVariable Long userId){
        List<NotificationResponseDto> notifications = notificationService.getNotificationByUser(userId);

        return ResponseEntity.ok(notifications);
    }


    @GetMapping("/user/{userId}/unread")
    public ResponseEntity<List<NotificationResponseDto>> getUnReadByUser(@PathVariable Long userId){
        List<NotificationResponseDto> unread = notificationService.getUnreadByUser(userId);

        return ResponseEntity.ok(unread);
    }


    @PostMapping("/{notificationId}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long notificationId){
        notificationService.markAsRead(notificationId);

        return ResponseEntity.noContent().build();
    }


    @PostMapping("/simulate/{userId}")
    public ResponseEntity<List<NotificationResponseDto>>simulateNotifications(@PathVariable Long userId){
        List<NotificationResponseDto> list = notificationService.createNotificationByUser(userId);

        return ResponseEntity.status(HttpStatus.CREATED).body(list);
    }


    @DeleteMapping
    public ResponseEntity<Void>deleteNotification(@PathVariable Long notificationId){
        notificationService.deleteNotification(notificationId);

        return ResponseEntity.noContent().build();
    }
}
