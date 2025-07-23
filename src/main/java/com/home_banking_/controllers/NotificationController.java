package com.home_banking_.controllers;

import com.home_banking_.dto.RequestDto.NotificationRequestDto;
import com.home_banking_.dto.ResponseDto.NotificationResponseDto;
import com.home_banking_.model.Notification;
import com.home_banking_.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.aspectj.weaver.ast.Not;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Notification controller", description = "Operations related with notifications ")
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;


    @Operation(
            summary = "Create a new notification",
            description = "Creates and stores a new notification in the system"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Notification created successfully.",
            content = @Content(mediaType = "application/json",
            array = @ArraySchema(schema = @Schema(implementation = NotificationResponseDto.class)))),
            @ApiResponse(responseCode = "400", description = "Invalid notification request")
    })
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<NotificationResponseDto> createNotification(@RequestBody @Valid NotificationRequestDto dto){

        NotificationResponseDto created = notificationService.createNotification(dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }



    @Operation(
            summary = "Get notifications by user.",
            description = "Returns a list notifications associated with the specified user account."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notification retrieved successfully",
            content = @Content(mediaType = "application/json",
            array = @ArraySchema(schema = @Schema(implementation = NotificationResponseDto.class)))),
            @ApiResponse(responseCode = "404", description = "No notifications found for the user")
    })
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN' , 'AUDITOR')")
    public ResponseEntity<List<NotificationResponseDto>> getNotificationByUser(
            @Parameter(name = "userId", description = "Unique identifier of the user.", required = true)
            @PathVariable Long userId){
        List<NotificationResponseDto> notifications = notificationService.getNotificationByUser(userId);

        return ResponseEntity.ok(notifications);
    }



    @Operation(
            summary = "Get unread notifications by user.",
            description = "Retrieves all unread notifications associated with the specified user account."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Unread notifications retrieved successfully.",
            content = @Content(mediaType = "application/json",
            array = @ArraySchema(schema = @Schema(implementation = NotificationResponseDto.class)))),
            @ApiResponse(responseCode = "404", description = "No unread notifications found for the user.")
    })
    @GetMapping("/user/{userId}/unread")
    @PreAuthorize("hasAnyRole('ADMIN' , 'EMPLOYED')")
    public ResponseEntity<List<NotificationResponseDto>> getUnReadByUser(
            @Parameter(name = "userId", description = "Unique identifier of the user", required = true)
            @PathVariable Long userId){

        List<NotificationResponseDto> unread = notificationService.getUnreadByUser(userId);

        return ResponseEntity.ok(unread);
    }




    @Operation(
            summary = "Mark a notification as read",
            description = "Sets the notification status to 'READ' based on the provided notification ID."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Notification marked as read successfully."),
            @ApiResponse(responseCode = "404", description = "Notification not found")
    })
    @PostMapping("/{notificationId}/read")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> markAsRead(
            @Parameter(name = "notificationId", description = "Unique identifier of the notification", required = true)
            @PathVariable Long notificationId){
        notificationService.markAsRead(notificationId);

        return ResponseEntity.noContent().build();
    }




    @Operation(
            summary = "Simulate notifications for user",
            description = "Creates and returns a list of simulated notifications for the specified user. Useful for testing or preview purpose."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Notifications simulated successfully.",
                    content= @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = NotificationResponseDto.class)))),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @PostMapping("/simulate/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<NotificationResponseDto>>simulateNotifications(@PathVariable Long userId){
        List<NotificationResponseDto> list = notificationService.createNotificationByUser(userId);

        return ResponseEntity.status(HttpStatus.CREATED).body(list);
    }




    @Operation(
            summary = "Delete a notification",
            description = "Deletes a notification permanently from the system based on its ID."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Notification deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Notification not found")
    })
    @DeleteMapping
    public ResponseEntity<Void>deleteNotification(
            @Parameter(name = "notificationId", description = "Unique identifier of the notification", required = true)
            @PathVariable Long notificationId){
        notificationService.deleteNotification(notificationId);

        return ResponseEntity.noContent().build();
    }
}
